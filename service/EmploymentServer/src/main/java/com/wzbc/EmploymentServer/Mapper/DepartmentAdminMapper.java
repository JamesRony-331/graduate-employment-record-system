package com.wzbc.EmploymentServer.Mapper;

import com.wzbc.EmploymentServer.entity.PO.DepartmentAdminPO;
import com.wzbc.EmploymentServer.entity.Query.PageQuery;
import com.wzbc.EmploymentServer.entity.VO.DepartmentAdmin.DepartmentAdminVO;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface DepartmentAdminMapper
{
    int insert(DepartmentAdminPO record);
    DepartmentAdminPO selectById(@Param("id") Long id);
    DepartmentAdminPO selectByIdForUpdate(@Param("id") Long id);
    DepartmentAdminVO selectDetail(@Param("id") Long id);
    List<DepartmentAdminVO> selectByPage(@Param("pageQuery") PageQuery query);
    int selectCount();
    int update(DepartmentAdminPO record);
    int deleteById(@Param("id") Long id);
}
