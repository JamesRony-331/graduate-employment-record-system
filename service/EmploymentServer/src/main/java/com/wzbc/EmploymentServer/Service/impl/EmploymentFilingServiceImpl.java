package com.wzbc.EmploymentServer.Service.impl;

import com.wzbc.EmploymentServer.Mapper.SysUserMapper;
import com.wzbc.EmploymentServer.Service.EmploymentAuditRecordService;
import com.wzbc.EmploymentServer.Service.SysUserRoleService;
import java.util.Objects;

import com.wzbc.EmploymentServer.Exception.BusinessException;
import com.wzbc.EmploymentServer.entity.DTO.EmploymentFilling.ReviewFilingDTO;
import com.wzbc.EmploymentServer.entity.PO.EmploymentAuditRecordPO;
import com.wzbc.EmploymentServer.Mapper.EmploymentFilingMapper;
import com.wzbc.EmploymentServer.Mapper.EmploymentFilingSequenceMapper;
import com.wzbc.EmploymentServer.Mapper.UserMapper;
import com.wzbc.EmploymentServer.Service.EmploymentFilingService;
import com.wzbc.EmploymentServer.entity.DTO.EmploymentFilling.AddEmployDTO;
import com.wzbc.EmploymentServer.entity.PO.EmploymentFilingPO;
import com.wzbc.EmploymentServer.entity.PO.UserPO;
import com.wzbc.EmploymentServer.entity.VO.EmploymentFiling.EmploymentFillingVO;
import com.wzbc.EmploymentServer.entity.VO.EmploymentFiling.EmploymentFilingManagementVO;
import com.wzbc.EmploymentServer.util.CopyTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.List;

@Service
public class EmploymentFilingServiceImpl implements EmploymentFilingService
{
    @Autowired
    private EmploymentFilingMapper employmentFilingMapper;

    @Autowired
    private EmploymentFilingSequenceMapper employmentFilingSequenceMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SysUserMapper sysUserMapper;
    @Autowired
    private SysUserRoleService userRoles;

    @Autowired
    private EmploymentAuditRecordService auditRecords;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateByCounselor(Long accountId, ReviewFilingDTO dto)
    {
        review(accountId, dto, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateByDepartment(Long accountId, ReviewFilingDTO dto)
    {
        review(accountId, dto, true);
    }

    private void review(Long accountId, ReviewFilingDTO dto, boolean department)
    {
        if (dto == null || dto.getFilingId() == null || dto.getFilingId() <= 0 || dto.getVersion() == null ||
            dto.getVersion() < 0 || dto.getFromStatus() == null)
            throw new BusinessException("请提供有效备案ID、版本和当前状态");
        // 与角色绑定操作使用相同账号锁，防止审核中途角色被替换。
        var user = sysUserMapper.selectByIdForUpdate(accountId);
        if (user == null || !Integer.valueOf(1).equals(user.getStatus()))
            throw new BusinessException(401, "账号不存在或已禁用");
        var codes = userRoles.selectEnabledRolesByUserId(accountId).stream().map(r -> r.getRoleCode()).toList();
        String required = department ? "DEPARTMENT_HEAD" : "COUNSELOR";
        if (!"STAFF".equals(user.getAccountType()) || codes.contains("STUDENT") || !codes.contains(required))
            throw new BusinessException(403, "无权执行此阶段审核");
        if (user.getRealName() == null || user.getRealName().isBlank())
            throw new BusinessException("请先完善审核人姓名");
        int expectedStatus = department ? 20 : 10;
        var filing = employmentFilingMapper.selectByIdForUpdate(dto.getFilingId());
        if (filing == null)
            throw new BusinessException("备案不存在");
        if (!Objects.equals(filing.getStatus(), dto.getFromStatus()) ||
            !Objects.equals(filing.getVersion(), dto.getVersion()) ||
            !Integer.valueOf(expectedStatus).equals(filing.getStatus()))
            throw new BusinessException(409, "备案状态或版本已变化，请刷新后重试");
        boolean allowed = employmentFilingMapper.selectManagedFilings(accountId, expectedStatus, department)
                                  .stream()
                                  .anyMatch(f -> f.getId().equals(dto.getFilingId()));
        if (!allowed)
            throw new BusinessException(403, "备案不在本人审核范围内");
        Integer result = dto.getAuditResult();
        if (!Integer.valueOf(1).equals(result) && !Integer.valueOf(2).equals(result))
            throw new BusinessException("审核结果只能为1通过或2打回");
        if (department && result == 2)
            throw new BusinessException("当前规则仅允许辅导员打回备案");
        if (dto.getAuditOpinion() != null && dto.getAuditOpinion().length() > 1000)
            throw new BusinessException("审核意见不能超过1000字");
        if (result == 2 && (dto.getAuditOpinion() == null || dto.getAuditOpinion().isBlank()))
            throw new BusinessException("打回时审核意见必填");
        int target = result == 2 ? 40 : (department ? 30 : 20);
        if (employmentFilingMapper.updateReviewStatus(filing.getId(), dto.getVersion(), expectedStatus, target) != 1)
            throw new BusinessException(409, "备案已被其他人更新，请刷新后重试");
        var record = new EmploymentAuditRecordPO();
        record.setFilingId(filing.getId())
        ;
        record.setAuditStage(department ? 2 : 1)
        ;
        record.setAuditorId(user.getId())
        ;
        record.setAuditorName(user.getRealName())
        ;
        record.setAuditorRole(department ? 2 : 1)
        ;
        record.setAuditResult(result)
        ;
        record.setAuditOpinion(dto.getAuditOpinion())
        ;
        record.setFromStatus(expectedStatus)
        ;
        record.setToStatus(target)
        ;
        auditRecords.add(record);
    }

    @Override
    public List<EmploymentFilingManagementVO> getPendingReviewFilings(Long accountId, Long userId)
    {
        boolean department = requireReviewRole(accountId, userId);
        return CopyTools.copyList(employmentFilingMapper.selectManagedFilings(userId, department ? 20 : 10, department),
                                  EmploymentFilingManagementVO.class);
    }

    @Override
    public List<EmploymentFilingManagementVO> getArchivedFilings(Long accountId, Long userId)
    {
        boolean department = requireReviewRole(accountId, userId);
        return CopyTools.copyList(employmentFilingMapper.selectManagedFilings(userId, 30, department),
                                  EmploymentFilingManagementVO.class);
    }

    private boolean requireReviewRole(Long accountId, Long userId)
    {
        if (userId == null || userId <= 0)
            throw new BusinessException("用户ID必须为正数");
        if (!Objects.equals(accountId, userId))
            throw new BusinessException(403, "只能查询本人管理范围内的备案");
        var user = sysUserMapper.selectById(userId);
        if (user == null || !Integer.valueOf(1).equals(user.getStatus()))
            throw new BusinessException(401, "账号不存在或已禁用");
        var codes = userRoles.selectEnabledRolesByUserId(userId).stream().map(r -> r.getRoleCode()).toList();
        if (!"STAFF".equals(user.getAccountType()) || codes.contains("STUDENT"))
            throw new BusinessException(403, "学生不能查询审核或归档管理列表");
        // 与班级、学生管理接口一致，多角色时优先使用院系管理员范围。
        if (codes.contains("DEPARTMENT_HEAD"))
            return true;
        if (codes.contains("COUNSELOR") && user.getCounselorId() != null)
            return false;
        throw new BusinessException(403, "需要有效的辅导员或院系管理员角色及资料关联");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addEmploymentFiling(Long UserUuid, AddEmployDTO addEmployDTO)
    {
        if (userMapper.lockStudent(UserUuid) == null)
            throw new BusinessException("学生不存在");
        var previous = employmentFilingMapper.selectLastFilingByStudentUuid(UserUuid);
        if (previous.stream().anyMatch(
                    f -> Integer.valueOf(10).equals(f.getStatus()) || Integer.valueOf(20).equals(f.getStatus())))
            throw new BusinessException("已有审核中的备案，不能提交新的申请");
        var last = previous.isEmpty() ? null : previous.getFirst();
        if (last != null && !Integer.valueOf(30).equals(last.getStatus()))
            throw new BusinessException("当前备案状态暂不支持新提交");
        if (last != null && (addEmployDTO.getChangeReason() == null || addEmployDTO.getChangeReason().isBlank() ||
                             addEmployDTO.getChangeReason().trim().length() > 1000))
            throw new BusinessException("请填写变更原因，最多1000字");
        UserPO userPO = userMapper.selectById(UserUuid);
        if (userPO == null)
        {
            throw new BusinessException("学生不存在");
        }
        EmploymentFilingPO employmentFilingPO = CopyTools.copy(addEmployDTO, EmploymentFilingPO.class);
        employmentFilingPO.setApplicationType(last == null ? 1 : 2);
        employmentFilingPO.setPreviousFilingId(last == null ? null : last.getId());
        employmentFilingPO.setChangeReason(last == null ? null : addEmployDTO.getChangeReason().trim());
        employmentFilingPO.setStudentUuid(userPO.getUuid());
        employmentFilingPO.setStudentNo(userPO.getStudentNo());
        employmentFilingPO.setStudentName(userPO.getName());
        employmentFilingPO.setCollege(userPO.getCollege());
        employmentFilingPO.setMajor(userPO.getMajor());
        employmentFilingPO.setClassId(userPO.getClassId());
        employmentFilingPO.setClassName(userPO.getClassName());
        employmentFilingPO.setGraduationYear(userPO.getGraduationYear());
        if (employmentFilingPO.getContactPhone() == null || employmentFilingPO.getContactPhone().isBlank())
        {
            employmentFilingPO.setContactPhone(userPO.getPhone());
        }
        if (employmentFilingPO.getContactEmail() == null || employmentFilingPO.getContactEmail().isBlank())
        {
            employmentFilingPO.setContactEmail(userPO.getEmail());
        }

        if (employmentFilingPO.getDestinationDetails() == null || employmentFilingPO.getDestinationDetails().isBlank())
        {
            employmentFilingPO.setDestinationDetails("{}");
        }
        employmentFilingPO.setMaterials("[]");
        if (employmentFilingPO.getRemark() == null)
        {
            employmentFilingPO.setRemark("");
        }
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
        String applicationDate = now.format(DateTimeFormatter.BASIC_ISO_DATE);
        employmentFilingSequenceMapper.incrementApplicationSequence(applicationDate);
        int sequence = employmentFilingSequenceMapper.selectApplicationSequence(applicationDate);
        if (sequence > 9999)
        {
            throw new BusinessException("当日申请编号已达上限，请次日再提交");
        }
        employmentFilingPO.setApplicationNo("JY" + applicationDate + String.format(Locale.ROOT, "%04d", sequence));
        employmentFilingPO.setStatus(10); // 待辅导员初审
        employmentFilingPO.setSubmittedAt(now);
        employmentFilingPO.setArchivedAt(null); // 审核通过后再设置归档时间
        employmentFilingPO.setVersion(0);
        employmentFilingPO.setCreatedAt(now);
        employmentFilingPO.setUpdatedAt(now);
        employmentFilingMapper.InsertEmploymentFiling(employmentFilingPO);
    }

    @Override
    public EmploymentFillingVO getFilingByStudentUuid(Long userUuid)
    {
        List<EmploymentFilingPO> filings = employmentFilingMapper.selectLastFilingByStudentUuid(userUuid);
        if (filings == null || filings.isEmpty())
        {
            return null;
        }
        EmploymentFilingPO lastFiling = filings.getFirst();
        return CopyTools.copy(lastFiling, EmploymentFillingVO.class);
    }

    @Override
    public List<EmploymentFillingVO> getFilingsByStudentUuid(Long userUuid)
    {
        List<EmploymentFilingPO> filings = employmentFilingMapper.selectLastFilingByStudentUuid(userUuid);
        if (filings == null || filings.isEmpty())
        {
            return null;
        }
        return CopyTools.copyList(filings, EmploymentFillingVO.class);
    }
}
