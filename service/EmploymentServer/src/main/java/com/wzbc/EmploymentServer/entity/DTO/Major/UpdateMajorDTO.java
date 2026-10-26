package com.wzbc.EmploymentServer.entity.DTO.Major;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdateMajorDTO
{
    @NotNull
    @Positive
    private Long id;
    @Positive
    private Long collegeId;
    @Size(min = 1, max = 100)
    private String majorName;
}
