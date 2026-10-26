package com.wzbc.EmploymentServer;

import com.wzbc.EmploymentServer.Service.impl.EmploymentFilingServiceImpl;
import com.wzbc.EmploymentServer.Mapper.*;
import com.wzbc.EmploymentServer.entity.PO.*;
import com.wzbc.EmploymentServer.entity.DTO.EmploymentFilling.AddEmployDTO;
import com.wzbc.EmploymentServer.Exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.List;

class ArchivedResubmissionTests
{
    @Test
    void archivedCreatesLinkedChangeAndPendingPreventsAnotherSubmission()
    {
        var service = new EmploymentFilingServiceImpl();
        var users = mock(UserMapper.class);
        var filings = mock(EmploymentFilingMapper.class);
        var sequence = mock(EmploymentFilingSequenceMapper.class);
        ReflectionTestUtils.setField(service, "userMapper", users);
        ReflectionTestUtils.setField(service, "employmentFilingMapper", filings);
        ReflectionTestUtils.setField(service, "employmentFilingSequenceMapper", sequence);
        when(users.lockStudent(1L)).thenReturn(1L);
        var student = new UserPO();
        student.setUuid(1L);
        when(users.selectById(1L)).thenReturn(student);
        var old = new EmploymentFilingPO();
        old.setId(8L);
        old.setStatus(30);
        when(filings.selectLastFilingByStudentUuid(1L)).thenReturn(List.of(old));
        var dto = new AddEmployDTO();
        assertThrows(BusinessException.class, () -> service.addEmploymentFiling(1L, dto));
        verify(filings, never()).InsertEmploymentFiling(any());
        dto.setChangeReason(" 更换工作单位 ");
        service.addEmploymentFiling(1L, dto);
        verify(filings).InsertEmploymentFiling(
                argThat(f
                        -> f.getApplicationType() == 2 && f.getPreviousFilingId() == 8L && f.getStatus() == 10 &&
                                   f.getChangeReason().equals("更换工作单位")));
        assertEquals(30, old.getStatus());
        old.setStatus(20);
        assertThrows(BusinessException.class, () -> service.addEmploymentFiling(1L, dto));
        old.setStatus(10);
        assertThrows(BusinessException.class, () -> service.addEmploymentFiling(1L, dto));
        verify(filings, times(1)).InsertEmploymentFiling(any());
    }
}
