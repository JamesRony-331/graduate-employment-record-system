package com.wzbc.EmploymentServer.Service.impl;

import com.wzbc.EmploymentServer.Service.ManagedClassService;
import com.wzbc.EmploymentServer.Service.SysUserRoleService;
import com.wzbc.EmploymentServer.Service.CounselorClassService;

import com.wzbc.EmploymentServer.Exception.BusinessException;
import com.wzbc.EmploymentServer.Mapper.ClassMapper;
import com.wzbc.EmploymentServer.Mapper.UserMapper;
import com.wzbc.EmploymentServer.util.CopyTools;
import com.wzbc.EmploymentServer.entity.VO.User.UserMsgVO;
import com.wzbc.EmploymentServer.Mapper.SysUserMapper;
import com.wzbc.EmploymentServer.entity.PO.ClassPO;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Objects;

/** 按当前账号的有效 RBAC 角色限定可查询班级范围。 */
@Service
public class ManagedClassServiceImpl implements ManagedClassService
{
    private final SysUserMapper users;
    private final SysUserRoleService roles;
    private final CounselorClassService counselorClasses;
    private final ClassMapper classes;
    private final UserMapper students;

    public ManagedClassServiceImpl(SysUserMapper users, SysUserRoleService roles,
                                   CounselorClassService counselorClasses, ClassMapper classes, UserMapper students)
    {
        this.users = users;
        this.roles = roles;
        this.counselorClasses = counselorClasses;
        this.classes = classes;
        this.students = students;
    }

    /** 先验证管理范围，再读取该班学生资料。 */
    @Override
    public List<UserMsgVO> selectStudentsByClassId(Long accountId, Long classId)
    {
        if (classId == null || classId <= 0)
        {
            throw new BusinessException("班级ID必须为正数");
        }
        boolean allowed = selectManagedClasses(accountId, null)
                                  .stream()
                                  .anyMatch(record -> Objects.equals(record.getId(), classId));
        if (!allowed)
        {
            throw new BusinessException(403, "班级不存在或不在本人管理范围内");
        }
        return CopyTools.copyList(students.selectByClassId(classId), UserMsgVO.class);
    }

    @Override
    public List<ClassPO> selectManagedClasses(Long accountId, Long requestedCounselorId)
    {
        var account = users.selectById(accountId);
        if (account == null || !Integer.valueOf(1).equals(account.getStatus()))
        {
            throw new BusinessException(401, "账号不存在或已禁用");
        }
        var roleCodes = roles.selectEnabledRolesByUserId(accountId).stream().map(role -> role.getRoleCode()).toList();
        // 学生优先拒绝，包括错误绑定了教职工角色的学生账号。
        if ("STUDENT".equals(account.getAccountType()) || roleCodes.contains("STUDENT"))
        {
            throw new BusinessException(403, "学生不能查询管理班级");
        }
        // 多角色时优先使用院系管理员的学院范围。
        if (roleCodes.contains("DEPARTMENT_HEAD"))
        {
            return classes.selectByDepartmentAdminUserId(accountId);
        }
        if (roleCodes.contains("COUNSELOR"))
        {
            Long counselorId = account.getCounselorId();
            if (counselorId == null ||
                (requestedCounselorId != null && !Objects.equals(counselorId, requestedCounselorId)))
            {
                throw new BusinessException(403, "只能查询本人管理的班级，请检查辅导员关联");
            }
            return counselorClasses.selectClassesByCounselorId(counselorId);
        }
        throw new BusinessException(403, "需要有效的辅导员或院系管理员角色");
    }
}
