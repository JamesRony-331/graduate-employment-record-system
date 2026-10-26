package com.wzbc.EmploymentServer.Mapper;

import com.wzbc.EmploymentServer.entity.PO.SysRolePO;
import com.wzbc.EmploymentServer.entity.Query.PageQuery;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface SysRoleMapper
{
    SysRolePO selectById(@Param("id") Long id);
    SysRolePO selectByIdForUpdate(@Param("id") Long id);
    SysRolePO selectByCode(@Param("roleCode") String roleCode);
    List<SysRolePO> selectAll();
    List<SysRolePO> selectByPage(@Param("pageQuery") PageQuery pageQuery);
    int selectCount();
    int insert(SysRolePO role);
    int update(SysRolePO role);
    int deleteById(@Param("id") Long id);
}
