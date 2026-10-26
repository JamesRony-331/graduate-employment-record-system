package com.wzbc.EmploymentServer.entity.VO.User;

import lombok.Data;

@Data
public class UserLoginVO
{
    private UserMsgVO userMsgVO;
    private String token;
}
