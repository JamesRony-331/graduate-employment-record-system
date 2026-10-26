package com.wzbc.EmploymentServer.entity.DTO.Notice;

import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class UpdateNoticeDTO
{
    @NotNull
    @Positive
    private Long id;
    @Size(min = 1, max = 200)
    private String title;
    private String content;
    @Min(1)
    @Max(4)
    private Integer noticeType;
    @Min(0)
    @Max(2)
    private Integer status;
    @Min(0)
    @Max(1)
    private Integer isTop;
}
