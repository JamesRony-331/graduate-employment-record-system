package com.wzbc.EmploymentServer.Service;

import com.wzbc.EmploymentServer.entity.PO.ClassPO;
import com.wzbc.EmploymentServer.entity.VO.User.UserMsgVO;
import java.util.List;

/** 按当前账号的有效 RBAC 角色限定可查询班级范围。 */
public interface ManagedClassService
{
    /** 查询账号管理的班级；requestedCounselorId 可为空，用于兼容旧接口。 */
    List<ClassPO> selectManagedClasses(Long accountId, Long requestedCounselorId);

    /** 校验管理范围后，查询指定班级的全部学生。 */
    List<UserMsgVO> selectStudentsByClassId(Long accountId, Long classId);
}
