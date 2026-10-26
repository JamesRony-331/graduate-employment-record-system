package com.wzbc.EmploymentServer.entity.DTO.EmploymentFilling;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ReviewFilingDTO
{
    @NotNull
    @Positive
    private Long filingId;
    @NotNull
    @Min(0)
    private Integer version;
    @NotNull
    private Integer fromStatus;
    /** 1通过，2打回；院系复审仅支持通过。 */
    @NotNull
    @Min(1)
    @Max(2)
    private Integer auditResult;
    @Size(max = 1000)
    private String auditOpinion;
}
