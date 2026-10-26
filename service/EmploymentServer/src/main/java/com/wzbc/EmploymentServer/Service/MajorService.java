package com.wzbc.EmploymentServer.Service;

import com.wzbc.EmploymentServer.entity.DTO.Major.*;
import com.wzbc.EmploymentServer.entity.PO.MajorPO;
import com.wzbc.EmploymentServer.entity.VO.PageResult;

public interface MajorService
{
    Long add(AddMajorDTO dto);
    MajorPO detail(Long id);
    PageResult<MajorPO> page(int pageNum, int pageSize);
    void update(UpdateMajorDTO dto);
    void delete(Long id);
}
