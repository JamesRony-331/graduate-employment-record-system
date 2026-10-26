package com.wzbc.EmploymentServer.entity.PO;

import lombok.Data;
import java.time.LocalDateTime;

/** 对应 sys_role。 */
@Data
public class SysRolePO
{
    private Long id;
    private String roleName;
    private String roleCode;
    /** 0禁用，1启用。 */
    private Integer status;
    private Integer sortOrder;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
