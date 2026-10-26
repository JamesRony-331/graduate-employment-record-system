package com.wzbc.EmploymentServer.Service;

import com.wzbc.EmploymentServer.entity.DTO.User.UpdateUserMsgAdmin;
import com.wzbc.EmploymentServer.entity.DTO.User.UserRegDTO;
import com.wzbc.EmploymentServer.entity.DTO.User.UserUpdateMsgDTO;

public interface StudentService
{
    void addStudent(UserRegDTO userRegDTO);

    void updateUserStu(Long uuid, UserUpdateMsgDTO userMsgDTO);

    void updateUserAdmin(UpdateUserMsgAdmin userMsgDTO);
}
