package com.wzbc.EmploymentServer.Service.impl;

import com.wzbc.EmploymentServer.Exception.BusinessException;
import com.wzbc.EmploymentServer.Mapper.*;
import com.wzbc.EmploymentServer.Service.*;
import com.wzbc.EmploymentServer.entity.DTO.DepartmentAdmin.*;
import com.wzbc.EmploymentServer.entity.DTO.SysUser.SysUserRegisterDTO;
import com.wzbc.EmploymentServer.entity.PO.DepartmentAdminPO;
import com.wzbc.EmploymentServer.entity.PO.SysUserPO;
import com.wzbc.EmploymentServer.entity.Query.PageQuery;
import com.wzbc.EmploymentServer.entity.VO.DepartmentAdmin.DepartmentAdminVO;
import com.wzbc.EmploymentServer.entity.VO.PageResult;
import com.wzbc.EmploymentServer.util.CopyTools;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DepartmentAdminServiceImpl implements DepartmentAdminService
{
    private final DepartmentAdminMapper mapper;
    private final CollegeMapper colleges;
    private final SysUserMapper users;
    private final SysRoleMapper roles;
    private final SysUserService accounts;
    private final SysUserRoleService bindings;

    public DepartmentAdminServiceImpl(DepartmentAdminMapper mapper, CollegeMapper colleges, SysUserMapper users,
                                      SysRoleMapper roles, SysUserService accounts, SysUserRoleService bindings)
    {
        this.mapper = mapper;
        this.colleges = colleges;
        this.users = users;
        this.roles = roles;
        this.accounts = accounts;
        this.bindings = bindings;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DepartmentAdminVO add(AddDepartmentAdminDTO dto)
    {
        requireCollege(dto.getCollegeId());
        var role = roles.selectByCode("DEPARTMENT_HEAD");
        if (role == null || !Integer.valueOf(1).equals(role.getStatus()))
        {
            throw new BusinessException("院系管理员角色DEPARTMENT_HEAD不存在或已禁用");
        }
        SysUserRegisterDTO registration = new SysUserRegisterDTO();
        registration.setUsername(dto.getUsername());
        registration.setPassword(dto.getPassword() == null ? "123456" : dto.getPassword());
        registration.setAccountType("STAFF");
        registration.setRealName(dto.getName());
        var account = accounts.register(registration);
        DepartmentAdminPO record = CopyTools.copy(dto, DepartmentAdminPO.class);
        record.setUserId(account.getId())
        ;
        if (mapper.insert(record) != 1 || record.getId() == null)
            throw new BusinessException("创建院系管理员失败");
        bindings.bind(account.getId(), role.getId());
        return selectById(record.getId());
    }

    @Override
    public PageResult<DepartmentAdminVO> selectByPage(int pageNum, int pageSize)
    {
        if (pageNum < 1 || pageSize < 1 || pageSize > 100 || (long)(pageNum - 1) * pageSize > Integer.MAX_VALUE)
        {
            throw new BusinessException("分页参数无效，页码从1开始，每页1至100条");
        }
        PageQuery query = new PageQuery(pageNum, pageSize, mapper.selectCount());
        return new PageResult<>(mapper.selectByPage(query), pageNum, pageSize, query.getTotalCount(), query.getTotal());
    }

    @Override
    public DepartmentAdminVO selectById(Long id)
    {
        checkId(id);
        var result = mapper.selectDetail(id);
        if (result == null)
            throw new BusinessException("院系管理员不存在");
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(UpdateDepartmentAdminDTO dto)
    {
        checkId(dto.getId());
        var record = mapper.selectByIdForUpdate(dto.getId());
        if (record == null)
            throw new BusinessException("院系管理员不存在");
        if (dto.getName() == null && dto.getCollegeId() == null)
            throw new BusinessException("请至少提供一个需要更新的字段");
        if (dto.getCollegeId() != null)
            requireCollege(dto.getCollegeId());
        var account = users.selectByIdForUpdate(record.getUserId());
        if (account == null || !Integer.valueOf(1).equals(account.getStatus()))
        {
            throw new BusinessException("关联账号不存在或已禁用");
        }
        mapper.update(CopyTools.copy(dto, DepartmentAdminPO.class));
        if (dto.getName() != null)
        {
            SysUserPO change = new SysUserPO();
            change.setId(record.getUserId());
            change.setRealName(dto.getName());
            users.update(change);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id)
    {
        checkId(id);
        var record = mapper.selectByIdForUpdate(id);
        if (record == null)
            throw new BusinessException("院系管理员不存在");
        users.selectByIdForUpdate(record.getUserId());
        users.softDelete(record.getUserId());
        for (var binding : bindings.selectByUserId(record.getUserId()))
        {
            bindings.unbind(record.getUserId(), binding.getRoleId());
        }
        if (mapper.deleteById(id) != 1)
            throw new BusinessException("删除院系管理员失败");
    }

    private void requireCollege(Long id)
    {
        if (id == null || id <= 0 || colleges.selectById(id) == null)
            throw new BusinessException("所属学院不存在");
    }

    private void checkId(Long id)
    {
        if (id == null || id <= 0)
            throw new BusinessException("院系管理员ID必须为正数");
    }
}
