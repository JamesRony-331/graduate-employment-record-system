package com.wzbc.EmploymentServer.entity.PO;

import lombok.Data;

@Data
public class ServiceEntryPO
{
    private Long id;
    private String name;
    private String iconUrl;
    private String url;
    private Integer sortOrder;
    private Integer status;
}
