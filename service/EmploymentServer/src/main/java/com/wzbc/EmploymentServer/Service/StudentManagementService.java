package com.wzbc.EmploymentServer.Service;

import java.util.List;
import com.wzbc.EmploymentServer.entity.DTO.Student.*;
import com.wzbc.EmploymentServer.entity.VO.User.UserMsgVO;
import com.wzbc.EmploymentServer.entity.VO.PageResult;

public interface StudentManagementService
{
    List<UserMsgVO> selectByUserId(Long accountId, Long userId);
    UserMsgVO add(Long accountId, AddStudentDTO dto);
    UserMsgVO detail(Long accountId, Long uuid);
    PageResult<UserMsgVO> page(Long accountId, int pageNum, int pageSize, Long collegeId, Long classId);
    void update(Long accountId, UpdateStudentDTO dto);
}
