package com.wzbc.EmploymentServer.Service;

import com.wzbc.EmploymentServer.entity.PO.SysRolePO;
import com.wzbc.EmploymentServer.entity.PO.SysUserRolePO;
import java.util.List;

public interface SysUserRoleService
{
    /** 绑定唯一的启用角色，替换其他角色；重复绑定同一角色保持原记录。 */
    void bind(Long userId, Long roleId);
    /** 解绑指定角色，重复解绑无副作用。 */
    void unbind(Long userId, Long roleId);
    List<SysUserRolePO> selectByUserId(Long userId);
    List<SysRolePO> selectEnabledRolesByUserId(Long userId);
}
