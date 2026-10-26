package com.wzbc.EmploymentServer;

import com.wzbc.EmploymentServer.Controller.ServiceEntryController;
import com.wzbc.EmploymentServer.Mapper.*;
import com.wzbc.EmploymentServer.entity.PO.*;
import com.wzbc.EmploymentServer.entity.DTO.ServiceEntry.ServiceEntryDTO;
import com.wzbc.EmploymentServer.Exception.BusinessException;
import org.junit.jupiter.api.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ServiceEntryTests
{
    ServiceEntryMapper mapper = mock(ServiceEntryMapper.class);
    SysUserRoleMapper roles = mock(SysUserRoleMapper.class);
    ServiceEntryController controller = new ServiceEntryController(mapper, roles);
    @BeforeEach
    void auth()
    {
        var user = new SysUserPO();
        user.setId(10L);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, List.of()));
    }
    @AfterEach
    void clear()
    {
        SecurityContextHolder.clearContext();
    }
    void admin()
    {
        var role = new SysRolePO();
        role.setRoleCode("ADMIN");
        when(roles.selectEnabledRolesByUserId(10L)).thenReturn(List.of(role));
    }
    ServiceEntryDTO dto()
    {
        var d = new ServiceEntryDTO();
        d.setName("服务");
        d.setIconUrl("/services/jobs.svg");
        d.setUrl("/jobs");
        d.setSortOrder(10);
        d.setStatus(1);
        return d;
    }
    @Test
    void nonAdminCannotReadOrMutateManagement()
    {
        when(roles.selectEnabledRolesByUserId(10L)).thenReturn(List.of());
        assertThrows(BusinessException.class, () -> controller.page(1, 10));
        assertThrows(BusinessException.class, () -> controller.detail(1L));
        assertThrows(BusinessException.class, () -> controller.add(dto()));
        assertThrows(BusinessException.class, () -> controller.update(dto()));
        assertThrows(BusinessException.class, () -> controller.delete(1L));
        verifyNoInteractions(mapper);
    }
    @Test
    void enabledUsesFilteredQuery()
    {
        when(mapper.enabled()).thenReturn(List.of());
        assertTrue(controller.enabled().getData().isEmpty());
        verify(mapper).enabled();
        verifyNoInteractions(roles);
    }
    @Test
    void adminCanCreateAndRejectsUnsafeUrls()
    {
        admin();
        assertEquals(200, controller.add(dto()).getCode());
        verify(mapper).insert(any());
        for (String url : List.of("javascript:alert(1)", "//evil.test", "/\\evil.test"))
        {
            var d = dto();
            d.setUrl(url);
            assertThrows(BusinessException.class, () -> controller.add(d));
        }
    }
    @Test
    void invalidPaginationAndMissingRecords()
    {
        admin();
        assertThrows(BusinessException.class, () -> controller.page(0, 10));
        assertThrows(BusinessException.class, () -> controller.page(1, 101));
        assertThrows(BusinessException.class, () -> controller.detail(1L));
        assertThrows(BusinessException.class, () -> controller.delete(1L));
    }
}
