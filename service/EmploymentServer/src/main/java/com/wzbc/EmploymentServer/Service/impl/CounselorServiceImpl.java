package com.wzbc.EmploymentServer.Service.impl;

import com.wzbc.EmploymentServer.Mapper.CounselorMapper;
import com.wzbc.EmploymentServer.Exception.BusinessException;
import com.wzbc.EmploymentServer.Service.CounselorService;
import com.wzbc.EmploymentServer.Service.SysUserService;
import com.wzbc.EmploymentServer.entity.DTO.SysUser.SysUserRegisterDTO;
import org.springframework.transaction.annotation.Transactional;
import com.wzbc.EmploymentServer.entity.DTO.Counselor.AddCounselorDTO;
import com.wzbc.EmploymentServer.entity.DTO.Counselor.UpdateCounselorDTO;
import com.wzbc.EmploymentServer.entity.PO.CounselorPO;
import com.wzbc.EmploymentServer.entity.Query.PageQuery;
import com.wzbc.EmploymentServer.entity.VO.Counselor.CounselorVO;
import com.wzbc.EmploymentServer.util.CopyTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CounselorServiceImpl implements CounselorService
{
    @Autowired
    private CounselorMapper counselorMapper;
    @Autowired
    private SysUserService sysUserService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addCouselor(AddCounselorDTO addCounselorTO)
    {
        if (counselorMapper.selectByCounselorNo(addCounselorTO.getCounselorNo()) != null)
        {
            throw new BusinessException("辅导员工号已存在");
        }
        CounselorPO counselorPO = CopyTools.copy(addCounselorTO, CounselorPO.class);
        counselorPO.setStatus(1);
        counselorPO.setCreatedAt(LocalDateTime.now());
        counselorPO.setUpdatedAt(LocalDateTime.now());
        if (counselorMapper.insert(counselorPO) != 1 || counselorPO.getId() == null)
        {
            throw new BusinessException("创建辅导员失败");
        }
        SysUserRegisterDTO account = new SysUserRegisterDTO();
        account.setUsername(addCounselorTO.getUsername() == null || addCounselorTO.getUsername().isBlank()
                                    ? addCounselorTO.getCounselorNo()
                                    : addCounselorTO.getUsername());
        account.setPassword(addCounselorTO.getPassword() == null ? "123456" : addCounselorTO.getPassword());
        account.setAccountType("STAFF");
        account.setRealName(counselorPO.getName());
        account.setCounselorId(counselorPO.getId());
        sysUserService.register(account);
    }

    @Override
    public List<CounselorVO> PageSelect(PageQuery pageQuery)
    {
        List<CounselorPO> counselorPOS = counselorMapper.SelectByPage(pageQuery);
        return CopyTools.copyList(counselorPOS, CounselorVO.class);
    }

    @Override
    public void UpdateCounselot(UpdateCounselorDTO updateCounselorDTO)
    {
        if (updateCounselorDTO.getGender() == null && updateCounselorDTO.getPhone() == null &&
            updateCounselorDTO.getEmail() == null && updateCounselorDTO.getStatus() == null)
        {
            throw new BusinessException("请至少提供一个需要更新的字段");
        }
        CounselorPO counselorPO = CopyTools.copy(updateCounselorDTO, CounselorPO.class);
        // MySQL 相同值更新可能返回0，需再确认记录是否存在。
        if (counselorMapper.update(counselorPO) == 0 && counselorMapper.selectById(counselorPO.getId()) == null)
        {
            throw new BusinessException("辅导员不存在");
        }
    }
}
