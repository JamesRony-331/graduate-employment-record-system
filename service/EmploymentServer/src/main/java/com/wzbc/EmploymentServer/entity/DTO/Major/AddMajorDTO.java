package com.wzbc.EmploymentServer.entity.DTO.Major;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class AddMajorDTO
{
    @NotNull
    @Positive
    private Long collegeId;
    @NotBlank
    @Size(max = 100)
    private String majorName;
}
