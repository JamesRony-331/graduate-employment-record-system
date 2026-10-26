package com.wzbc.EmploymentServer.Mapper;

import com.wzbc.EmploymentServer.entity.PO.SysRolePO;
import com.wzbc.EmploymentServer.entity.PO.SysUserRolePO;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface SysUserRoleMapper
{
    List<SysUserRolePO> selectByUserId(@Param("userId") Long userId);
    /** 仅返回账号正常且角色启用的角色，按排序值、ID排列。 */
    List<SysRolePO> selectEnabledRolesByUserId(@Param("userId") Long userId);
    /** 重复绑定不新增记录。 */
    int insert(SysUserRolePO binding);
    int deleteByUserIdAndRoleId(@Param("userId") Long userId, @Param("roleId") Long roleId);
    int deleteOtherRoles(@Param("userId") Long userId, @Param("roleId") Long roleId);
    int countByRoleId(@Param("roleId") Long roleId);
}
