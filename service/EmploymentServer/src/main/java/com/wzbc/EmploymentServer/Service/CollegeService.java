package com.wzbc.EmploymentServer.Service;

import com.wzbc.EmploymentServer.entity.DTO.College.UpdateCollegeDTO;
import com.wzbc.EmploymentServer.entity.DTO.College.AddCollegeDTO;
import com.wzbc.EmploymentServer.entity.PO.CollegePO;
import com.wzbc.EmploymentServer.entity.VO.PageResult;

public interface CollegeService
{
    Long add(AddCollegeDTO dto);
    PageResult<CollegePO> selectByPage(int pageNum, int pageSize);
    CollegePO selectById(Long id);
    void update(UpdateCollegeDTO dto);
    void delete(Long id);
}
