package com.wzbc.EmploymentServer.entity.DTO.DepartmentAdmin;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class AddDepartmentAdminDTO
{
    @NotBlank(message = "登录账号不能为空")
    @Size(max = 50)
    private String username;
    /** 未传或为null时默认123456。 */
    @Size(min = 6, max = 72)
    private String password;
    @NotBlank(message = "姓名不能为空")
    @Size(max = 50)
    private String name;
    @NotNull(message = "所属学院不能为空")
    @Positive
    private Long collegeId;
}
