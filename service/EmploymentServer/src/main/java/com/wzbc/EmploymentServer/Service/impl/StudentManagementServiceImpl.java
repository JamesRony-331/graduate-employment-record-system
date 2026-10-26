package com.wzbc.EmploymentServer.Service.impl;

import com.wzbc.EmploymentServer.Service.*;
import com.wzbc.EmploymentServer.Mapper.*;
import com.wzbc.EmploymentServer.Exception.BusinessException;
import com.wzbc.EmploymentServer.entity.DTO.Student.*;
import com.wzbc.EmploymentServer.entity.DTO.SysUser.SysUserRegisterDTO;
import com.wzbc.EmploymentServer.entity.VO.User.UserMsgVO;
import com.wzbc.EmploymentServer.entity.VO.PageResult;
import com.wzbc.EmploymentServer.entity.PO.UserPO;
import com.wzbc.EmploymentServer.util.CopyTools;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Objects;

@Service
public class StudentManagementServiceImpl implements StudentManagementService
{
    private final ManagedClassService scope;
    private final SysUserService accounts;
    private final StudentService students;
    private final UserMapper mapper;
    private final SysUserMapper users;
    public StudentManagementServiceImpl(ManagedClassService scope, SysUserService accounts, StudentService students,
                                        UserMapper mapper, SysUserMapper users)
    {
        this.scope = scope;
        this.accounts = accounts;
        this.students = students;
        this.mapper = mapper;
        this.users = users;
    }
    @Override
    public List<UserMsgVO> selectByUserId(Long accountId, Long userId)
    {
        if (userId == null || userId <= 0)
            throw new BusinessException("用户ID必须为正数");
        if (!Objects.equals(accountId, userId))
        {
            throw new BusinessException(403, "只能查询当前登录账号管理范围内的学生");
        }
        List<Long> classIds = scope.selectManagedClasses(userId, null).stream().map(c -> c.getId()).toList();
        if (classIds.isEmpty())
            return List.of();
        return CopyTools.copyList(mapper.selectByClassIds(classIds), UserMsgVO.class);
    }

    private void requireClass(Long accountId, Long classId)
    {
        if (classId == null ||
            scope.selectManagedClasses(accountId, null).stream().noneMatch(c -> Objects.equals(c.getId(), classId)))
            throw new BusinessException(403, "班级不在本人管理范围内");
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserMsgVO add(Long accountId, AddStudentDTO dto)
    {
        requireClass(accountId, dto.getClassId());
        var registration = new SysUserRegisterDTO();
        String idCard = dto.getIdCard();
        if (idCard == null || !idCard.matches("[0-9]{17}[0-9Xx]"))
            throw new BusinessException("请填写18位身份证号");
        registration.setUsername(dto.getStudentNo());
        registration.setPassword(idCard.substring(idCard.length() - 6));
        registration.setAccountType("STUDENT");
        registration.setStudent(dto);
        var account = accounts.register(registration);
        return detail(accountId, account.getStudentUuid());
    }
    @Override
    public UserMsgVO detail(Long accountId, Long uuid)
    {
        if (uuid == null || uuid <= 0)
            throw new BusinessException("学生ID必须为正数");
        var student = mapper.selectById(uuid);
        if (student == null)
            throw new BusinessException("学生不存在");
        requireClass(accountId, student.getClassId());
        return CopyTools.copy(student, UserMsgVO.class);
    }
    @Override
    public PageResult<UserMsgVO> page(Long accountId, int pageNum, int pageSize, Long collegeId, Long classId)
    {
        if (pageNum < 1 || pageSize < 1 || pageSize > 100 || (long)(pageNum - 1) * pageSize > Integer.MAX_VALUE)
            throw new BusinessException("分页参数无效，页码从1开始，每页1至100条");
        if ((collegeId != null && collegeId <= 0) || (classId != null && classId <= 0))
            throw new BusinessException("学院和班级ID必须为正数");
        List<Long> ids = scope.selectManagedClasses(accountId, null)
                                 .stream()
                                 .filter(c -> collegeId == null || Objects.equals(c.getCollegeId(), collegeId))
                                 .filter(c -> classId == null || Objects.equals(c.getId(), classId))
                                 .map(c -> c.getId())
                                 .toList();
        if (ids.isEmpty())
            return new PageResult<>(List.of(), pageNum, pageSize, 0, 0);
        int count = mapper.countByClassIds(ids);
        var records = CopyTools.copyList(mapper.selectPageByClassIds(ids, (pageNum - 1) * pageSize, pageSize),
                                         UserMsgVO.class);
        return new PageResult<>(records, pageNum, pageSize, count, (int)((count + (long)pageSize - 1) / pageSize));
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long accountId, UpdateStudentDTO dto)
    {
        detail(accountId, dto.getUuid());
        if (dto.getClassId() != null)
            requireClass(accountId, dto.getClassId());
        students.updateUserAdmin(dto);
        if (dto.getName() != null)
        {
            var change = new UserPO();
            change.setUuid(dto.getUuid());
            change.setName(dto.getName().trim());
            if (mapper.UpdateUser(change) != 1)
                throw new BusinessException("学生信息更新失败");
            if (users.updateStudentRealName(dto.getUuid(), change.getName()) != 1)
                throw new BusinessException("学生关联账号不存在或异常");
        }
    }
}
