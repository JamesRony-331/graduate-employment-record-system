package com.wzbc.EmploymentServer.entity.DTO.SysUser;

import com.wzbc.EmploymentServer.entity.DTO.User.UserRegDTO;
import jakarta.validation.Valid;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class SysUserRegisterDTO
{
    @NotBlank(message = "账号不能为空")
    @Size(max = 50)
    private String username;
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 72, message = "密码长度应为6到72个字符")
    private String password;
    @Positive
    private Long counselorId;
    @Size(max = 50)
    private String realName;
    @Pattern(regexp = "STUDENT|STAFF")
    @NotNull
    private String accountType = "STAFF";
    @Valid
    private UserRegDTO student;
}
