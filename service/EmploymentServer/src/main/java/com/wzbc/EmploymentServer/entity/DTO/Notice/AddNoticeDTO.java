package com.wzbc.EmploymentServer.entity.DTO.Notice;

import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class AddNoticeDTO
{
    @NotBlank
    @Size(max = 200)
    private String title;
    @NotBlank
    private String content;
    @NotNull
    @Min(1)
    @Max(4)
    private Integer noticeType;
    @NotNull
    @Min(0)
    @Max(2)
    private Integer status = 0;
    @NotNull
    @Min(0)
    @Max(1)
    private Integer isTop = 0;
}
