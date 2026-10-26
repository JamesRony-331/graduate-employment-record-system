package com.wzbc.EmploymentServer.entity.DTO.Class;

import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class UpdateClassDTO
{
    @NotNull
    @Positive
    private Long id;
    @Size(max = 30)
    @Pattern(regexp = "(?s).*\\S.*", message = "编号或名称不能为空白")
    private String classCode;
    @Size(max = 100)
    @Pattern(regexp = "(?s).*\\S.*", message = "编号或名称不能为空白")
    private String className;
    @Positive
    private Long collegeId;
    @Positive
    private Long majorId;
    @Min(1900)
    @Max(9999)
    private Integer enrollmentYear;
    @Min(0)
    private Integer studentCount;
}
