package com.wzbc.EmploymentServer.Service.impl;

import com.wzbc.EmploymentServer.Exception.BusinessException;
import com.wzbc.EmploymentServer.Mapper.SysRoleMapper;
import com.wzbc.EmploymentServer.Mapper.SysUserMapper;
import com.wzbc.EmploymentServer.Mapper.SysUserRoleMapper;
import com.wzbc.EmploymentServer.Service.SysUserRoleService;
import com.wzbc.EmploymentServer.entity.PO.SysRolePO;
import com.wzbc.EmploymentServer.entity.PO.SysUserRolePO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class SysUserRoleServiceImpl implements SysUserRoleService
{
    private final SysUserRoleMapper mapper;
    private final SysUserMapper users;
    private final SysRoleMapper roles;

    public SysUserRoleServiceImpl(SysUserRoleMapper mapper, SysUserMapper users, SysRoleMapper roles)
    {
        this.mapper = mapper;
        this.users = users;
        this.roles = roles;
    }

    @Override
    @Transactional
    public void bind(Long userId, Long roleId)
    {
        checkId(userId, "账号");
        checkId(roleId, "角色");
        var user = users.selectByIdForUpdate(userId);
        if (user == null || !Integer.valueOf(1).equals(user.getStatus()))
        {
            throw new BusinessException("账号不存在或已禁用");
        }
        var role = roles.selectByIdForUpdate(roleId);
        if (role == null || !Integer.valueOf(1).equals(role.getStatus()))
        {
            throw new BusinessException("角色不存在或已禁用");
        }
        // 目标角色校验通过后再替换旧绑定，同角色绑定保留原记录。
        mapper.deleteOtherRoles(userId, roleId);
        var binding = new SysUserRolePO();
        binding.setUserId(userId);
        binding.setRoleId(roleId);
        mapper.insert(binding);
    }

    @Override
    @Transactional
    public void unbind(Long userId, Long roleId)
    {
        checkId(userId, "账号");
        checkId(roleId, "角色");
        users.selectByIdForUpdate(userId);
        mapper.deleteByUserIdAndRoleId(userId, roleId);
    }

    @Override
    public List<SysUserRolePO> selectByUserId(Long userId)
    {
        checkId(userId, "账号");
        return mapper.selectByUserId(userId);
    }

    @Override
    public List<SysRolePO> selectEnabledRolesByUserId(Long userId)
    {
        checkId(userId, "账号");
        return mapper.selectEnabledRolesByUserId(userId);
    }

    private void checkId(Long id, String label)
    {
        if (id == null || id <= 0)
            throw new BusinessException(label + "ID必须为正数");
    }
}
