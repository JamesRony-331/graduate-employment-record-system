package com.wzbc.EmploymentServer.entity.DTO.College;

import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class AddCollegeDTO
{
    @Size(max = 20)
    @Pattern(regexp = "(?s).*\\S.*", message = "编号或名称不能为空白")
    @NotBlank
    private String collegeCode;
    @Size(max = 100)
    @Pattern(regexp = "(?s).*\\S.*", message = "编号或名称不能为空白")
    @NotBlank
    private String collegeName;
    @Size(max = 50)
    private String deanName;
    @Size(max = 20)
    private String phone;
    @Size(max = 500)
    private String description;
}
