package com.wzbc.EmploymentServer.Service;

import com.wzbc.EmploymentServer.entity.DTO.CounselorClass.*;
import com.wzbc.EmploymentServer.entity.PO.CounselorClassPO;
import com.wzbc.EmploymentServer.entity.PO.ClassPO;
import java.util.List;
import com.wzbc.EmploymentServer.entity.VO.PageResult;

public interface CounselorClassService
{
    List<ClassPO> selectClassesByCounselorId(Long counselorId);
    Long add(AddCounselorClassDTO dto);
    CounselorClassPO selectById(Long id);
    PageResult<CounselorClassPO> selectByPage(int pageNum, int pageSize);
    void update(UpdateCounselorClassDTO dto);
    void delete(Long id);
}
