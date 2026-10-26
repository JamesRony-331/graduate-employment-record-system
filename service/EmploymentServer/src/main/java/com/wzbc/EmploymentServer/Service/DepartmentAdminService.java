package com.wzbc.EmploymentServer.Service;

import com.wzbc.EmploymentServer.entity.DTO.DepartmentAdmin.*;
import com.wzbc.EmploymentServer.entity.VO.DepartmentAdmin.DepartmentAdminVO;
import com.wzbc.EmploymentServer.entity.VO.PageResult;

public interface DepartmentAdminService
{
    DepartmentAdminVO add(AddDepartmentAdminDTO dto);
    PageResult<DepartmentAdminVO> selectByPage(int pageNum, int pageSize);
    DepartmentAdminVO selectById(Long id);
    void update(UpdateDepartmentAdminDTO dto);
    void delete(Long id);
}
