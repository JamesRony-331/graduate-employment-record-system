package com.wzbc.EmploymentServer.entity.DTO.CounselorClass;

import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class UpdateCounselorClassDTO
{
    @NotNull
    @Positive
    private Long id;
    @Positive
    private Long counselorId;
    @Positive
    private Long classId;
    @Min(1)
    @Max(2)
    private Integer roleType;
}
