package com.wzbc.EmploymentServer.entity.VO.SysUser;

import com.wzbc.EmploymentServer.entity.VO.User.UserMsgVO;

import lombok.Data;

@Data
public class SysUserLoginVO
{
    private String token;
    private UserMsgVO studentMsgVO;
    private SysUserLoginMsgVO userMsgVO;
}
