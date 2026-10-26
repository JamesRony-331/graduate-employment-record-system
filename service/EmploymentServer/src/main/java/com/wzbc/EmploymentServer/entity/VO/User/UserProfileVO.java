package com.wzbc.EmploymentServer.entity.VO.User;

import com.wzbc.EmploymentServer.entity.VO.SysUser.SysUserMsgVO.RoleVO;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户个人信息，沿用 UserMsgVO 的个人资料。
 * 学院使用 collegeId、college，班级使用 classId、className。
 */
@Getter
@Setter
public class UserProfileVO extends UserMsgVO
{
    /** sys_user.id，与继承的学生 uuid 区分。 */
    private Long userId;

    /** RBAC 绑定角色，包含角色 ID、编码、名称和状态；不直接代表有效权限。 */
    private List<RoleVO> roles = new ArrayList<>();
}
