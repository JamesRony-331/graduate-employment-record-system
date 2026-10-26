package com.wzbc.EmploymentServer.Service;

import com.wzbc.EmploymentServer.entity.DTO.Class.UpdateClassDTO;
import com.wzbc.EmploymentServer.entity.DTO.Class.AddClassDTO;
import com.wzbc.EmploymentServer.entity.PO.ClassPO;
import com.wzbc.EmploymentServer.entity.VO.PageResult;

public interface ClassService
{
    Long add(AddClassDTO dto);
    PageResult<ClassPO> selectByPage(int pageNum, int pageSize);
    ClassPO selectById(Long id);
    void update(UpdateClassDTO dto);
    /** 绑定学院，已有绑定时覆盖。 */
    void bindCollege(Long classId, Long collegeId);
    void delete(Long id);
}
