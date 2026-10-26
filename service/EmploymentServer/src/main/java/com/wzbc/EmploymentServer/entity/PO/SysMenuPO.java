package com.wzbc.EmploymentServer.entity.PO;

import lombok.Data;

@Data
public class SysMenuPO
{
    private Long id;
    private Long parentId;
    private String name;
    private String path;
    private String icon;
    private String menuType;
    private String permission;
    private String clientType;
    private Integer sortOrder;
    private Integer status;
}
