package com.wzbc.EmploymentServer.entity.PO;

import lombok.Data;
import java.time.LocalDateTime;

/** 对应 sys_user_role，Service 保证一个账号只绑定一个角色。 */
@Data
public class SysUserRolePO
{
    private Long id;
    private Long userId;
    private Long roleId;
    private LocalDateTime createdAt;
}
