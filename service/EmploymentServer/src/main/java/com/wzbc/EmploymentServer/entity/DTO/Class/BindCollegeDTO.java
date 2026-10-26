package com.wzbc.EmploymentServer.entity.DTO.Class;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class BindCollegeDTO
{
    @NotNull(message = "班级ID不能为空")
    @Positive(message = "班级ID必须为正数")
    private Long classId;

    @NotNull(message = "学院ID不能为空")
    @Positive(message = "学院ID必须为正数")
    private Long collegeId;
}
