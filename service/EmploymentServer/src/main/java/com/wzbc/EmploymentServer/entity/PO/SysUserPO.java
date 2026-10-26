package com.wzbc.EmploymentServer.entity.PO;

import lombok.Data;
import java.time.LocalDateTime;

/** 统一登录账号，对应 sys_user。 */
@Data
public class SysUserPO
{
    private Long id;
    private String username;
    private String password;
    private String passwordAlgorithm;
    private Long counselorId;
    /** 关联 student.uuid，教职工账号为空。 */
    private Long studentUuid;
    /** STUDENT 或 STAFF；具体教职工权限由后续角色体系决定。 */
    private String accountType;
    private String realName;
    /** 0禁用，1正常。 */
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
