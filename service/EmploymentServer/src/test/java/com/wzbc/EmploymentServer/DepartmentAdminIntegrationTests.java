package com.wzbc.EmploymentServer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wzbc.EmploymentServer.Service.*;
import com.wzbc.EmploymentServer.entity.DTO.SysUser.*;
import com.wzbc.EmploymentServer.entity.DTO.DepartmentAdmin.*;
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
                 "spring.datasource.url=jdbc:h2:mem:department-tests;MODE=MySQL;DB_CLOSE_DELAY=-1",
                 "spring.datasource.username=sa", "spring.datasource.password=",
                 "spring.datasource.driver-class-name=org.h2.Driver", "spring.sql.init.mode=always",
                 "spring.sql.init.schema-locations=classpath:account-schema.sql,classpath:department-admin-schema.sql"
                 })
@AutoConfigureMockMvc
class DepartmentAdminIntegrationTests
{
    @Autowired
    DepartmentAdminService service;
    @Autowired
    SysUserService accounts;
    @Autowired
    CollegeService colleges;
    @Autowired
    JdbcTemplate jdbc;
    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper json;

    @BeforeEach
    void reset()
    {
        jdbc.update("DELETE FROM college_admin");
        jdbc.update("DELETE FROM sys_user_role");
        jdbc.update("DELETE FROM sys_user");
        jdbc.update("DELETE FROM sys_role");
        jdbc.update("DELETE FROM college");
        jdbc.update("INSERT INTO college(id,college_code,college_name) VALUES(1,'C1','学院一'),(2,'C2','学院二')");
        jdbc.update("INSERT INTO sys_role(role_code,role_name) VALUES('DEPARTMENT_HEAD','院系管理员')");
    }

    AddDepartmentAdminDTO request(String username)
    {
        var dto = new AddDepartmentAdminDTO();
        dto.setUsername(username);
        dto.setName("王老师");
        dto.setCollegeId(1L);
        return dto;
    }

    String login(String username, String password)
    {
        var dto = new SysUserLoginDTO();
        dto.setUsername(username);
        dto.setPassword(password);
        dto.setAccountType("STAFF");
        return accounts.login(dto).getToken();
    }

    @Test
    void addBindsRoleUpdateSynchronizesAndDeleteDisablesLogin() throws Exception
    {
        var created = service.add(request("department1"));
        assertNotNull(created.getId());
        assertEquals("学院一", created.getCollegeName());
        assertEquals(
                "DEPARTMENT_HEAD",
                jdbc.queryForObject(
                        "SELECT r.role_code FROM sys_user_role ur JOIN sys_role r ON r.id=ur.role_id WHERE ur.user_id=?",
                        String.class, created.getUserId()));
        String token = login("department1", "123456");
        var change = new UpdateDepartmentAdminDTO();
        change.setId(created.getId());
        change.setName("李老师");
        change.setCollegeId(2L);
        service.update(change);
        assertEquals("李老师", service.selectById(created.getId()).getName());
        assertEquals("李老师", jdbc.queryForObject("SELECT real_name FROM sys_user WHERE id=?", String.class,
                                                   created.getUserId()));
        assertEquals(2L, service.selectById(created.getId()).getCollegeId());
        assertThrows(RuntimeException.class, () -> colleges.delete(2L));
        service.delete(created.getId());
        assertEquals(0,
                     jdbc.queryForObject("SELECT status FROM sys_user WHERE id=?", Integer.class, created.getUserId()));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM sys_user_role WHERE user_id=?", Integer.class,
                                            created.getUserId()));
        assertThrows(RuntimeException.class, () -> login("department1", "123456"));
        mvc.perform(get("/department-admin/page").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
        assertThrows(RuntimeException.class, () -> service.selectById(created.getId()));
    }

    @Test
    void creationFailureRollsBackAndInvalidReferencesAreRejected()
    {
        var invalid = request("bad");
        invalid.setName("模拟资料失败");
        assertThrows(RuntimeException.class, () -> service.add(invalid));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM sys_user", Integer.class));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM college_admin", Integer.class));
        invalid.setName("王老师");
        invalid.setCollegeId(999L);
        assertThrows(RuntimeException.class, () -> service.add(invalid));
        invalid.setCollegeId(1L);
        jdbc.update("UPDATE sys_role SET status=0");
        assertThrows(RuntimeException.class, () -> service.add(invalid));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM sys_user", Integer.class));
    }

    @Test
    void controllerSupportsCreationPaginationUpdateAndDeletion() throws Exception
    {
        var staff = new SysUserRegisterDTO();
        staff.setUsername("operator");
        staff.setPassword("Password@123");
        accounts.register(staff);
        String token = login("operator", "Password@123");
        mvc.perform(post("/department-admin/add")
                            .header("Authorization", "Bearer " + token)
                            .contentType("application/json")
                            .content(json.writeValueAsString(request("first"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("first"))
                .andExpect(jsonPath("$.data.password").doesNotExist());
        var second = service.add(request("second"));
        mvc.perform(get("/department-admin/page")
                            .param("pageNum", "2")
                            .param("pageSize", "1")
                            .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.data.totalCount").value(2))
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.records[0].username").value("first"));
        var change = new UpdateDepartmentAdminDTO();
        change.setId(second.getId());
        change.setCollegeId(2L);
        mvc.perform(put("/department-admin/update")
                            .header("Authorization", "Bearer " + token)
                            .contentType("application/json")
                            .content(json.writeValueAsString(change)))
                .andExpect(jsonPath("$.code").value(200));
        mvc.perform(delete("/department-admin/" + second.getId()).header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.code").value(200));
        assertEquals(1, service.selectByPage(1, 10).totalCount());
        mvc.perform(get("/department-admin/page")).andExpect(status().isUnauthorized());
        assertThrows(RuntimeException.class, () -> service.selectByPage(0, 10));
    }
}
