package com.wzbc.EmploymentServer.entity.DTO.Menu;

import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class MenuDTO
{
    @PositiveOrZero
    private Long parentId = 0L;
    @NotBlank
    @Size(max = 50)
    private String name;
    @Size(max = 200)
    @Pattern(regexp = "^$|^/[A-Za-z0-9/_-]*$")
    private String path;
    @Size(max = 50)
    private String icon;
    @NotBlank
    @Pattern(regexp = "MENU|BUTTON|DIRECTORY")
    private String menuType;
    @NotBlank
    @Size(max = 100)
    private String permission;
    @NotBlank
    @Pattern(regexp = "ADMIN|STUDENT")
    private String clientType;
    @NotNull
    @Min(0)
    private Integer sortOrder = 0;
    @NotNull
    @Min(0)
    @Max(1)
    private Integer status = 1;
}
