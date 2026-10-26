package com.wzbc.EmploymentServer.entity.DTO.SysUser;

import jakarta.validation.constraints.*;
import lombok.Data;

/** 只修改当前登录账号；null 表示不修改对应字段。 */
@Data
public class SysUserUpdateDTO
{
    @Size(min = 1, max = 50)
    private String username;
    @Size(min = 6, max = 72)
    private String password;
    @Positive
    private Long counselorId;
    @Size(max = 50)
    private String realName;
}
