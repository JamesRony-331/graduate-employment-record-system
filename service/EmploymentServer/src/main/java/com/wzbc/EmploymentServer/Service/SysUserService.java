package com.wzbc.EmploymentServer.Service;

import com.wzbc.EmploymentServer.entity.VO.User.UserProfileVO;

import com.wzbc.EmploymentServer.entity.VO.PageResult;

import com.wzbc.EmploymentServer.entity.DTO.SysUser.*;
import com.wzbc.EmploymentServer.entity.VO.SysUser.*;

public interface SysUserService
{
    UserProfileVO getProfile(Long userId);
    PageResult<SysUserMsgVO> SelectByPage(int pageNum, int pageSize);
    SysUserMsgVO register(SysUserRegisterDTO dto);
    SysUserLoginVO login(SysUserLoginDTO dto);
    void updateInfo(Long id, SysUserUpdateDTO dto);
    void softDelete(Long id);
}
