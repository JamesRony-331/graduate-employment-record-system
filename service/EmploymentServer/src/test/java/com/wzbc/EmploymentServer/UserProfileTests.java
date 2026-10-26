package com.wzbc.EmploymentServer;

import com.wzbc.EmploymentServer.Mapper.SysUserMapper;
import com.wzbc.EmploymentServer.Mapper.UserMapper;
import com.wzbc.EmploymentServer.Service.SysUserRoleService;
import com.wzbc.EmploymentServer.Service.impl.SysUserServiceImpl;
import com.wzbc.EmploymentServer.entity.PO.SysUserPO;
import com.wzbc.EmploymentServer.entity.PO.SysRolePO;
import com.wzbc.EmploymentServer.entity.PO.UserPO;
import com.wzbc.EmploymentServer.Exception.BusinessException;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class UserProfileTests
{
    @Test
    void studentProfileIncludesSchoolClassAndRbacRole()
    {
        var accounts = mock(SysUserMapper.class);
        var students = mock(UserMapper.class);
        var roles = mock(SysUserRoleService.class);
        var service = new SysUserServiceImpl(accounts, null, students, null, roles);
        var account = new SysUserPO();
        account.setId(10L);
        account.setStatus(1);
        account.setAccountType("STUDENT");
        account.setStudentUuid(20L);
        when(accounts.selectById(10L)).thenReturn(account);
        var student = new UserPO();
        student.setUuid(20L);
        student.setName("张同学");
        student.setCollegeId(3L);
        student.setCollege("信息学院");
        student.setClassId(4L);
        student.setClassName("软件一班");
        when(students.selectById(20L)).thenReturn(student);
        var role = new SysRolePO();
        role.setId(1L);
        role.setRoleCode("STUDENT");
        role.setRoleName("学生");
        role.setStatus(1);
        when(roles.selectEnabledRolesByUserId(10L)).thenReturn(List.of(role));
        var profile = service.getProfile(10L);
        assertEquals(10L, profile.getUserId());
        assertEquals(20L, profile.getUuid());
        assertEquals("信息学院", profile.getCollege());
        assertEquals("软件一班", profile.getClassName());
        assertEquals("STUDENT", profile.getRoles().getFirst().getRoleCode());
        assertThrows(BusinessException.class, () -> service.getProfile(99L));
        assertThrows(BusinessException.class, () -> service.getProfile(null));
    }
}
