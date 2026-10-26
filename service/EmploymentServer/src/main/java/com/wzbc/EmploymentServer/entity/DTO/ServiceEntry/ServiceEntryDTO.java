package com.wzbc.EmploymentServer.entity.DTO.ServiceEntry;

import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class ServiceEntryDTO
{
    @Positive
    private Long id;
    @NotBlank
    @Size(max = 50)
    private String name;
    @NotBlank
    @Size(max = 500)
    private String iconUrl;
    @NotBlank
    @Size(max = 500)
    private String url;
    @NotNull
    @Min(0)
    @Max(99999)
    private Integer sortOrder;
    @NotNull
    @Min(0)
    @Max(1)
    private Integer status;
}
