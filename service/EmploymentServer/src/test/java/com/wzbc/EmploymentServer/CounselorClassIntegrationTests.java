package com.wzbc.EmploymentServer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wzbc.EmploymentServer.Service.*;
import com.wzbc.EmploymentServer.entity.DTO.SysUser.*;
import com.wzbc.EmploymentServer.entity.DTO.CounselorClass.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(
        properties =
                {"spring.config.import=",
                 "spring.datasource.url=jdbc:h2:mem:counselor-class-tests;MODE=MySQL;DB_CLOSE_DELAY=-1",
                 "spring.datasource.username=sa", "spring.datasource.password=",
                 "spring.datasource.driver-class-name=org.h2.Driver", "spring.sql.init.mode=always",
                 "spring.sql.init.schema-locations=classpath:account-schema.sql,classpath:counselor-class-schema.sql"
                 })
@AutoConfigureMockMvc
class CounselorClassIntegrationTests
{
    @Autowired
    CounselorClassService service;
    @Autowired
    ClassService classes;
    @Autowired
    SysUserService accounts;
    @Autowired
    JdbcTemplate jdbc;
    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper json;

    @BeforeEach
    void reset()
    {
        jdbc.update("DELETE FROM counselor_class");
        jdbc.update("DELETE FROM sys_user_role");
        jdbc.update("DELETE FROM sys_user");
        jdbc.update("DELETE FROM counselor");
        jdbc.update("DELETE FROM class_info");
        jdbc.update("INSERT INTO counselor(id,counselor_no,name,status) VALUES(1,'C1','老师一',1),(2,'C2','老师二',1)");
        jdbc.update(
                "INSERT INTO class_info(id,class_code,class_name,college_id,student_count) VALUES(1,'B1','班级一',1,10),(2,'B2','班级二',1,20)");
    }

    AddCounselorClassDTO request(long counselorId, long classId, int role)
    {
        var dto = new AddCounselorClassDTO();
        dto.setCounselorId(counselorId);
        dto.setClassId(classId);
        dto.setRoleType(role);
        return dto;
    }

    String token()
    {
        var registration = new SysUserRegisterDTO();
        registration.setUsername("staff");
        registration.setPassword("Password@123");
        accounts.register(registration);
        var login = new SysUserLoginDTO();
        login.setUsername("staff");
        login.setPassword("Password@123");
        login.setAccountType("STAFF");
        return accounts.login(login).getToken();
    }

    @Test
    void crudPreservesAssignmentTimeAndProtectsClass()
    {
        Long id = service.add(request(1, 1, 1));
        var original = service.selectById(id);
        assertNotNull(original.getAssignedAt());
        assertThrows(RuntimeException.class, () -> service.add(request(1, 1, 2)));
        assertThrows(RuntimeException.class, () -> classes.delete(1L));
        var change = new UpdateCounselorClassDTO();
        change.setId(id);
        change.setRoleType(2);
        service.update(change);
        assertEquals(2, service.selectById(id).getRoleType());
        assertEquals(1L, service.selectById(id).getCounselorId());
        assertEquals(original.getAssignedAt(), service.selectById(id).getAssignedAt());
        Long second = service.add(request(2, 2, 1));
        var page = service.selectByPage(2, 1);
        assertEquals(2, page.totalCount());
        assertEquals(2, page.total());
        assertEquals(id, page.records().getFirst().getId());
        change.setCounselorId(2L);
        change.setClassId(2L);
        assertThrows(RuntimeException.class, () -> service.update(change));
        assertEquals(1L, service.selectById(id).getClassId());
        service.delete(id);
        classes.delete(1L);
        assertThrows(RuntimeException.class, () -> service.selectById(id));
        assertNotNull(service.selectById(second));
    }

    @Test
    void invalidReferencesAndPayloadsAreRejected()
    {
        assertThrows(RuntimeException.class, () -> service.add(request(999, 1, 1)));
        assertThrows(RuntimeException.class, () -> service.add(request(1, 999, 1)));
        assertThrows(RuntimeException.class, () -> service.add(request(1, 1, 3)));
        Long id = service.add(request(1, 1, 1));
        var change = new UpdateCounselorClassDTO();
        change.setId(id);
        assertThrows(RuntimeException.class, () -> service.update(change));
        assertThrows(RuntimeException.class, () -> service.selectByPage(0, 10));
        assertThrows(RuntimeException.class, () -> service.delete(999L));
    }

    @Test
    void defaultRoleAndCounselorClassLookup() throws Exception
    {
        String token = token();
        mvc.perform(post("/counselor-class/add")
                            .header("Authorization", "Bearer " + token)
                            .contentType("application/json")
                            .content("{\"counselorId\":1,\"classId\":1}"))
                .andExpect(jsonPath("$.code").value(200));
        assertEquals(1, service.selectByPage(1, 10).records().getFirst().getRoleType());
        var dto = request(1, 2, 2);
        dto.setRoleType(null);
        Long second = service.add(dto);
        assertEquals(1, service.selectById(second).getRoleType());
        var change = new UpdateCounselorClassDTO();
        change.setId(second);
        change.setRoleType(2);
        service.update(change);
        mvc.perform(get("/counselor-class/by-counselor/1").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].className").value("班级二"))
                .andExpect(jsonPath("$.data[1].id").value(1));
        assertTrue(service.selectClassesByCounselorId(2L).isEmpty());
        assertThrows(RuntimeException.class, () -> service.selectClassesByCounselorId(999L));
    }

    @Test
    void endpointsSupportCrudAndValidation() throws Exception
    {
        String token = token();
        var result = mvc.perform(post("/counselor-class/add")
                                         .header("Authorization", "Bearer " + token)
                                         .contentType("application/json")
                                         .content(json.writeValueAsString(request(1, 1, 1))))
                             .andExpect(status().isOk())
                             .andExpect(jsonPath("$.code").value(200))
                             .andReturn();
        long id = json.readTree(result.getResponse().getContentAsString()).get("data").asLong();
        mvc.perform(get("/counselor-class/" + id).header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.data.counselorId").value(1));
        mvc.perform(get("/counselor-class/page").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.data.totalCount").value(1));
        var change = new UpdateCounselorClassDTO();
        change.setId(id);
        change.setRoleType(2);
        mvc.perform(put("/counselor-class/update")
                            .header("Authorization", "Bearer " + token)
                            .contentType("application/json")
                            .content(json.writeValueAsString(change)))
                .andExpect(jsonPath("$.code").value(200));
        mvc.perform(post("/counselor-class/add")
                            .header("Authorization", "Bearer " + token)
                            .contentType("application/json")
                            .content("{}"))
                .andExpect(jsonPath("$.code").value(600));
        mvc.perform(delete("/counselor-class/" + id).header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.code").value(200));
        mvc.perform(get("/counselor-class/page")).andExpect(status().isUnauthorized());
    }
}
