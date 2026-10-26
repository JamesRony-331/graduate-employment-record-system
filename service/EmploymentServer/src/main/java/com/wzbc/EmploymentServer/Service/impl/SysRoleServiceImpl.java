package com.wzbc.EmploymentServer.Service.impl;

import com.wzbc.EmploymentServer.Exception.BusinessException;
import com.wzbc.EmploymentServer.Mapper.SysRoleMapper;
import com.wzbc.EmploymentServer.Mapper.SysUserRoleMapper;
import com.wzbc.EmploymentServer.Service.SysRoleService;
import com.wzbc.EmploymentServer.entity.PO.SysRolePO;
import com.wzbc.EmploymentServer.entity.Query.PageQuery;
import com.wzbc.EmploymentServer.entity.VO.PageResult;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class SysRoleServiceImpl implements SysRoleService
{
    private final SysRoleMapper mapper;
    private final SysUserRoleMapper bindings;

    public SysRoleServiceImpl(SysRoleMapper mapper, SysUserRoleMapper bindings)
    {
        this.mapper = mapper;
        this.bindings = bindings;
    }

    @Override
    public Long add(SysRolePO role)
    {
        validate(role, true);
        if (mapper.selectByCode(role.getRoleCode()) != null)
            throw new BusinessException("角色编码已存在");
        try
        {
            mapper.insert(role);
        }
        catch (DuplicateKeyException e)
        {
            throw new BusinessException("角色编码已存在");
        }
        return role.getId();
    }

    @Override
    public SysRolePO selectById(Long id)
    {
        if (id == null || id <= 0)
            throw new BusinessException("角色ID必须为正数");
        SysRolePO role = mapper.selectById(id);
        if (role == null)
            throw new BusinessException("角色不存在");
        return role;
    }

    @Override
    public List<SysRolePO> selectAll()
    {
        return mapper.selectAll();
    }

    @Override
    public PageResult<SysRolePO> selectByPage(int pageNum, int pageSize)
    {
        if (pageNum < 1 || pageSize < 1 || pageSize > 100 || (long)(pageNum - 1) * pageSize > Integer.MAX_VALUE)
        {
            throw new BusinessException("分页参数无效，页码从1开始，每页1至100条");
        }
        PageQuery query = new PageQuery(pageNum, pageSize, mapper.selectCount());
        return new PageResult<>(mapper.selectByPage(query), pageNum, pageSize, query.getTotalCount(), query.getTotal());
    }

    @Override
    @Transactional
    public void update(SysRolePO role)
    {
        validate(role, false);
        selectById(role.getId());
        if (role.getRoleCode() != null)
        {
            SysRolePO existing = mapper.selectByCode(role.getRoleCode());
            if (existing != null && !existing.getId().equals(role.getId()))
            {
                throw new BusinessException("角色编码已存在");
            }
        }
        try
        {
            if (mapper.update(role) == 0)
                selectById(role.getId());
        }
        catch (DuplicateKeyException e)
        {
            throw new BusinessException("角色编码已存在");
        }
    }

    @Override
    @Transactional
    public void delete(Long id)
    {
        if (id == null || id <= 0)
            throw new BusinessException("角色ID必须为正数");
        if (mapper.selectByIdForUpdate(id) == null)
            throw new BusinessException("角色不存在");
        if (bindings.countByRoleId(id) > 0)
            throw new BusinessException("角色已绑定账号，不能删除");
        if (mapper.deleteById(id) == 0)
            throw new BusinessException("角色不存在");
    }

    private void validate(SysRolePO role, boolean creating)
    {
        if (role == null)
            throw new BusinessException("角色信息不能为空");
        if ((creating || role.getRoleName() != null) &&
            (role.getRoleName() == null || role.getRoleName().isBlank() || role.getRoleName().length() > 50))
        {
            throw new BusinessException("角色名称不能为空且不能超过50个字符");
        }
        if ((creating || role.getRoleCode() != null) &&
            (role.getRoleCode() == null || role.getRoleCode().isBlank() || role.getRoleCode().length() > 50))
        {
            throw new BusinessException("角色编码不能为空且不能超过50个字符");
        }
        if (role.getStatus() != null && role.getStatus() != 0 && role.getStatus() != 1)
        {
            throw new BusinessException("角色状态只能为0或1");
        }
        if (role.getRemark() != null && role.getRemark().length() > 500)
            throw new BusinessException("备注不能超过500个字符");
        if (!creating && role.getRoleName() == null && role.getRoleCode() == null && role.getStatus() == null &&
            role.getSortOrder() == null && role.getRemark() == null)
        {
            throw new BusinessException("请至少提供一个需要更新的字段");
        }
    }
}
