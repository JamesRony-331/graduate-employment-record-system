package com.wzbc.EmploymentServer.entity.DTO.DepartmentAdmin;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdateDepartmentAdminDTO
{
    @NotNull
    @Positive
    private Long id;
    @Size(max = 50)
    @Pattern(regexp = "(?s).*\\S.*", message = "姓名不能为空白")
    private String name;
    @Positive
    private Long collegeId;
}
