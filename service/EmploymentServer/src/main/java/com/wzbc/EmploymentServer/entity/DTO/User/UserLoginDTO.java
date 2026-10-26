package com.wzbc.EmploymentServer.entity.DTO.User;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
public class UserLoginDTO
{
    private String studentNo;
    private String password;
}
