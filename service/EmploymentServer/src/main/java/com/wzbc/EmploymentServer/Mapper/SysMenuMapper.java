package com.wzbc.EmploymentServer.Mapper;

import com.wzbc.EmploymentServer.entity.PO.*;
import org.apache.ibatis.annotations.*;
import java.util.List;

public interface SysMenuMapper
{
    String C =
            "m.id,m.parent_id AS parentId,m.name,m.path,m.icon,m.menu_type AS menuType,m.permission,m.client_type AS clientType,m.sort_order AS sortOrder,m.status";
    @Select("SELECT " + C + " FROM sys_menu m ORDER BY m.sort_order,m.id")
    List<SysMenuPO> all();
    @Select("SELECT " + C + " FROM sys_menu m WHERE m.id=#{id}")
    SysMenuPO detail(Long id);
    @Select("SELECT DISTINCT " + C +
            " FROM sys_menu m JOIN sys_role_menu rm ON rm.menu_id=m.id JOIN sys_role r ON r.id=rm.role_id JOIN sys_user_role ur ON ur.role_id=r.id WHERE ur.user_id=#{userId} AND r.status=1 AND m.status=1 AND m.client_type=#{clientType} ORDER BY m.sort_order,m.id")
    List<SysMenuPO>
    mine(@Param("userId") Long userId, @Param("clientType") String clientType);
    @Insert("INSERT INTO sys_menu(parent_id,name,path,icon,menu_type,permission,client_type,sort_order,status) VALUES(#{parentId},#{name},#{path},#{icon},#{menuType},#{permission},#{clientType},#{sortOrder},#{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SysMenuPO row);
    @Update("UPDATE sys_menu SET parent_id=#{parentId},name=#{name},path=#{path},icon=#{icon},menu_type=#{menuType},permission=#{permission},client_type=#{clientType},sort_order=#{sortOrder},status=#{status} WHERE id=#{id}")
    int update(SysMenuPO row);
    @Select("SELECT COUNT(*) FROM sys_menu WHERE parent_id=#{id}")
    int children(Long id);
    @Delete("DELETE FROM sys_menu WHERE id=#{id}")
    int delete(Long id);
    @Select("SELECT id,role_name AS roleName,role_code AS roleCode,status FROM sys_role ORDER BY sort_order,id")
    List<SysRolePO> roles();
    @Select("SELECT menu_id FROM sys_role_menu WHERE role_id=#{id}")
    List<Long> granted(Long id);
    @Delete("DELETE FROM sys_role_menu WHERE role_id=#{id}")
    void clearRole(Long id);
    @Insert("INSERT INTO sys_role_menu(role_id,menu_id) VALUES(#{roleId},#{menuId})")
    void grant(@Param("roleId") Long roleId, @Param("menuId") Long menuId);
}
