package com.wzbc.EmploymentServer.entity.DTO.WorkSelect;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class WorkSkillDTO
{
    private Long id;
    @NotBlank
    @Size(max = 100)
    private String name;
}
