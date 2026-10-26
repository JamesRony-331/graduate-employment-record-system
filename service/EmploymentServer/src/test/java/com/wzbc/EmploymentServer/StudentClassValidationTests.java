package com.wzbc.EmploymentServer;

import com.wzbc.EmploymentServer.Mapper.*;
import com.wzbc.EmploymentServer.Service.impl.StudentServiceImpl;
import com.wzbc.EmploymentServer.Exception.BusinessException;
import com.wzbc.EmploymentServer.entity.DTO.User.*;
import com.wzbc.EmploymentServer.entity.PO.*;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.List;

class StudentClassValidationTests
{
    @Test
    void creationRejectsMissingAndUnknownClassBeforeInsert()
    {
        var service = new StudentServiceImpl();
        var users = mock(UserMapper.class);
        var classes = mock(ClassMapper.class);
        ReflectionTestUtils.setField(service, "userMapper", users);
        ReflectionTestUtils.setField(service, "classMapper", classes);
        var dto = new UserRegDTO();
        dto.setStudentNo("20260001");
        dto.setIdCard("330302200001010011");
        assertThrows(BusinessException.class, () -> service.addStudent(dto));
        dto.setClassId(999L);
        assertThrows(BusinessException.class, () -> service.addStudent(dto));
        verify(users, never()).InsertUser(any());
        var classroom = new ClassPO();
        classroom.setMajorId(3L);
        classroom.setCollegeId(2L);
        when(classes.selectByIdForUpdate(1L)).thenReturn(classroom);
        when(users.SelectUserByStudentNoOrIdCard(any(), isNull())).thenReturn(List.of());
        dto.setClassId(1L);
        dto.setMajorId(4L);
        assertThrows(BusinessException.class, () -> service.addStudent(dto));
        dto.setMajorId(null);
        dto.setCollegeId(9L);
        assertThrows(BusinessException.class, () -> service.addStudent(dto));
        dto.setCollegeId(null);
        service.addStudent(dto);
        verify(users).InsertUser(argThat(u -> u.getClassId() == 1L && u.getMajorId() == 3L && u.getCollegeId() == 2L));
    }
    @Test
    void adminCannotChangeStudentToUnknownClass()
    {
        var service = new StudentServiceImpl();
        var users = mock(UserMapper.class);
        var classes = mock(ClassMapper.class);
        ReflectionTestUtils.setField(service, "userMapper", users);
        ReflectionTestUtils.setField(service, "classMapper", classes);
        when(users.selectById(1L)).thenReturn(new UserPO());
        var dto = new UpdateUserMsgAdmin();
        dto.setUuid(1L);
        dto.setClassId(999L);
        assertThrows(BusinessException.class, () -> service.updateUserAdmin(dto));
        verify(users, never()).UpdateUser(any());
    }
    @Test
    void identityUpdateRefreshesDerivedFieldsAndRejectsInvalidDates()
    {
        var service = new StudentServiceImpl();
        var users = mock(UserMapper.class);
        ReflectionTestUtils.setField(service, "userMapper", users);
        var existing = new UserPO();
        existing.setEnrollmentYear(2024);
        existing.setGraduationYear(2028);
        when(users.selectById(1L)).thenReturn(existing);
        when(users.UpdateUser(any())).thenReturn(1);
        var dto = new UpdateUserMsgAdmin();
        dto.setUuid(1L);
        dto.setIdCard("330302200002300011");
        assertThrows(BusinessException.class, () -> service.updateUserAdmin(dto));
        verify(users, never()).UpdateUser(any());
        dto.setIdCard("330302200001010011");
        service.updateUserAdmin(dto);
        verify(users).UpdateUser(argThat(u -> u.getGender() == 1 && u.getBirthDate().toString().equals("2000-01-01")));
        dto.setGraduationYear(2020);
        assertThrows(BusinessException.class, () -> service.updateUserAdmin(dto));
    }
}
