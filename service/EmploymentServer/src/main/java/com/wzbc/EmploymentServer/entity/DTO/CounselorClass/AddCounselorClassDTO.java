package com.wzbc.EmploymentServer.entity.DTO.CounselorClass;

import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class AddCounselorClassDTO
{
    @NotNull
    @Positive
    private Long counselorId;
    @NotNull
    @Positive
    private Long classId;
    /** 未传或为null时默认1（主辅导员）。 */
    @Min(1)
    @Max(2)
    private Integer roleType = 1;
}
