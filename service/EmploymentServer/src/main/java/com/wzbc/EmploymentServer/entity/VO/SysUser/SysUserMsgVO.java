package com.wzbc.EmploymentServer.entity.VO.SysUser;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** 账号信息响应，不包含密码哈希。 */
@Data
public class SysUserMsgVO
{
    /** 分页查询返回的已绑定角色，包含停用角色及其状态，不代表有效权限。 */
    private List<RoleVO> roles = new ArrayList<>();

    @Data
    public static class RoleVO
    {
        private Long id;
        private String roleCode;
        private String roleName;
        private Integer status;
    }

    private Long id;
    private String username;
    private Long counselorId;
    /** 关联 student.uuid，教职工账号为空。 */
    private Long studentUuid;
    /** STUDENT 或 STAFF；具体身份由 RBAC roles 表示。 */
    private String accountType;
    private String realName;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
