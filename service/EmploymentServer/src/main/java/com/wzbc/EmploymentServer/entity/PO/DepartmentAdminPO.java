package com.wzbc.EmploymentServer.entity.PO;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DepartmentAdminPO
{
    private Long id;
    private Long userId;
    private String name;
    private Long collegeId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
