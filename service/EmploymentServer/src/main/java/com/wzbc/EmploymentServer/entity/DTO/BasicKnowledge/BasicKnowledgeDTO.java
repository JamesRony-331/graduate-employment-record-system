package com.wzbc.EmploymentServer.entity.DTO.BasicKnowledge;

import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class BasicKnowledgeDTO
{
    @Positive
    private Long id;
    @NotBlank
    @Size(max = 200)
    private String title;
    @NotBlank
    @Size(max = 10000)
    private String answer;
    @NotNull
    @Min(0)
    @Max(1)
    private Integer status;
}
