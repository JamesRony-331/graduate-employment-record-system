package com.wzbc.EmploymentServer.entity.VO.SysUser;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

/** 登录账号信息：对外展示辅导员姓名，隐藏内部关联ID。 */
@Getter
@Setter
public class SysUserLoginMsgVO extends SysUserMsgVO
{
    private String counselorName;

    @Override
    @JsonIgnore
    public Long getCounselorId()
    {
        return super.getCounselorId();
    }
}
