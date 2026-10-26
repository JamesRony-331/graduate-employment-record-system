package com.wzbc.EmploymentServer.entity.VO.DepartmentAdmin;

import lombok.Data;
import java.time.LocalDateTime;

/** 管理员资料及账号展示信息，不包含密码。 */
@Data
public class DepartmentAdminVO
{
    private Long id;
    private Long userId;
    private String username;
    private Integer status;
    private String name;
    private Long collegeId;
    private String collegeName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
