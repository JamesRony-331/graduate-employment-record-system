package com.wzbc.EmploymentServer.entity.DTO.SysUser;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SysUserLoginDTO
{
    @NotBlank(message = "账号不能为空")
    @Size(max = 50, message = "账号不能超过50个字符")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
    /** 登录入口类型只是校验条件，实际身份以数据库记录为准。 */
    @Pattern(regexp = "STUDENT|STAFF")
    @NotNull
    private String accountType = "STAFF";
}
