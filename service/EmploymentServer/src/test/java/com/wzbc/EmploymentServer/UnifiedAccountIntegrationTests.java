package com.wzbc.EmploymentServer;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wzbc.EmploymentServer.Service.SysUserService;
import com.wzbc.EmploymentServer.entity.DTO.SysUser.*;
import com.wzbc.EmploymentServer.entity.DTO.User.UserRegDTO;
import com.wzbc.EmploymentServer.entity.DTO.Token.TokenUserDTO;
import com.wzbc.EmploymentServer.entity.VO.SysUser.SysUserLoginVO;
import com.wzbc.EmploymentServer.util.StringTools;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties =
{"spring.config.import=",
                              "spring.datasource.url=jdbc:h2:mem:account-tests;MODE=MySQL;DB_CLOSE_DELAY=-1",
                              "spring.datasource.username=sa", "spring.datasource.password=",
                              "spring.datasource.driver-class-name=org.h2.Driver", "spring.sql.init.mode=always",
                              "spring.sql.init.schema-locations=classpath:account-schema.sql"
                              })
@AutoConfigureMockMvc
class UnifiedAccountIntegrationTests
{
    @Autowired
    SysUserService service;
    @Autowired
    JdbcTemplate jdbc;
    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper json;
    @BeforeEach
    void reset()
    {
        jdbc.update("DELETE FROM sys_user_role");
        jdbc.update("DELETE FROM sys_role");
        jdbc.update("INSERT INTO sys_role(role_name,role_code) VALUES ('学生','STUDENT')");
        jdbc.update("DELETE FROM sys_user");
        jdbc.update("DELETE FROM student");
        jdbc.update("DELETE FROM counselor");
    }
    SysUserRegisterDTO registration(String username, String type)
    {
        var dto = new SysUserRegisterDTO();
        dto.setUsername(username);
        dto.setPassword("Password@123");
        dto.setAccountType(type);
        dto.setRealName("测试老师");
        if (type.equals("STUDENT"))
        {
            var student = new UserRegDTO();
            student.setStudentNo(username);
            student.setName("测试学生");
            student.setIdCard("330302200001011234");
            student.setCollegeId(1L);
            student.setClassId(999L);
            dto.setStudent(student);
        }
        return dto;
    }
    SysUserLoginDTO credentials(String name, String type, String password)
    {
        var dto = new SysUserLoginDTO();
        dto.setUsername(name);
        dto.setAccountType(type);
        dto.setPassword(password);
        return dto;
    }
    String staffToken()
    {
        service.register(registration("staff", "STAFF"));
        return service.login(credentials("staff", "STAFF", "Password@123")).getToken();
    }
    @Test
    void accountPaginationReturnsSafeRecordsAndEnforcesStaffAccess() throws Exception
    {
        String staff = staffToken();
        var disabled = service.register(registration("disabled-page", "STAFF"));
        service.softDelete(disabled.getId());
        mvc.perform(get("/sys-user/page")
                            .param("pageNum", "1")
                            .param("pageSize", "1")
                            .header("Authorization", "Bearer " + staff))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalCount").value(2))
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.records.length()").value(1))
                .andExpect(jsonPath("$.data.records[0].username").value("disabled-page"))
                .andExpect(jsonPath("$.data.records[0].status").value(0))
                .andExpect(jsonPath("$.data.records[0].password").doesNotExist())
                .andExpect(jsonPath("$.data.records[0].passwordAlgorithm").doesNotExist());
        mvc.perform(get("/sys-user/SelectByPage")
                            .param("pageNum", "2")
                            .param("pageSize", "1")
                            .header("Authorization", "Bearer " + staff))
                .andExpect(jsonPath("$.data.records[0].username").value("staff"));
        mvc.perform(get("/sys-user/page").param("pageNum", "0").header("Authorization", "Bearer " + staff))
                .andExpect(jsonPath("$.code").value(600));
        mvc.perform(get("/sys-user/page")).andExpect(status().isUnauthorized());
        service.register(registration("page-student", "STUDENT"));
        String student = service.login(credentials("page-student", "STUDENT", "Password@123")).getToken();
        mvc.perform(get("/sys-user/page").header("Authorization", "Bearer " + student))
                .andExpect(status().isForbidden());
        mvc.perform(get("/sys-user/SelectByPage").header("Authorization", "Bearer " + student))
                .andExpect(status().isForbidden());
    }

    @Test
    void accountPaginationIncludesAllBoundRolesWithoutSplittingUsers() throws Exception
    {
        String staff = staffToken();
        var user = service.register(registration("multi-role", "STAFF"));
        jdbc.update(
                "INSERT INTO sys_role(role_name,role_code,status,sort_order) VALUES ('辅导员','COUNSELOR',1,10),('院系审核员','DEPARTMENT_REVIEWER',0,20)");
        jdbc.update(
                "INSERT INTO sys_user_role(user_id,role_id) SELECT ?,id FROM sys_role WHERE role_code IN ('COUNSELOR','DEPARTMENT_REVIEWER')",
                user.getId());
        mvc.perform(get("/sys-user/page").param("pageSize", "1").header("Authorization", "Bearer " + staff))
                .andExpect(jsonPath("$.data.totalCount").value(2))
                .andExpect(jsonPath("$.data.records.length()").value(1))
                .andExpect(jsonPath("$.data.records[0].username").value("multi-role"))
                .andExpect(jsonPath("$.data.records[0].roles.length()").value(2))
                .andExpect(jsonPath("$.data.records[0].roles[0].roleCode").value("COUNSELOR"))
                .andExpect(jsonPath("$.data.records[0].roles[0].roleName").value("辅导员"))
                .andExpect(jsonPath("$.data.records[0].roles[1].status").value(0));
        mvc.perform(get("/sys-user/SelectByPage")
                            .param("pageNum", "2")
                            .param("pageSize", "1")
                            .header("Authorization", "Bearer " + staff))
                .andExpect(jsonPath("$.data.records[0].username").value("staff"))
                .andExpect(jsonPath("$.data.records[0].roles").isEmpty());
    }

    @Test
    void loginReturnsCounselorNameInsteadOfCounselorId() throws Exception
    {
        var account = service.register(registration("named-counselor", "STAFF"));
        jdbc.update("INSERT INTO counselor(id,counselor_no,name,status) VALUES(777,'C777','辅导员姓名',1)");
        jdbc.update("UPDATE sys_user SET counselor_id=777 WHERE id=?", account.getId());
        mvc.perform(
                   post("/sys-user/login")
                           .contentType("application/json")
                           .content(
                                   "{\"username\":\"named-counselor\",\"password\":\"Password@123\",\"accountType\":\"STAFF\"}"))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.userMsgVO.counselorName").value("辅导员姓名"))
                .andExpect(jsonPath("$.data.userMsgVO.counselorId").doesNotExist())
                .andExpect(jsonPath("$.data.userMsgVO.password").doesNotExist());
        jdbc.update("UPDATE sys_user SET counselor_id=NULL WHERE id=?", account.getId());
        mvc.perform(
                   post("/sys-user/login")
                           .contentType("application/json")
                           .content(
                                   "{\"username\":\"named-counselor\",\"password\":\"Password@123\",\"accountType\":\"STAFF\"}"))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.userMsgVO.counselorName").isEmpty())
                .andExpect(jsonPath("$.data.userMsgVO.counselorId").doesNotExist());
    }

    @Test
    void addingCounselorCreatesBoundLoginAccount() throws Exception
    {
        String staff = staffToken();
        mvc.perform(post("/counselor/add-counselor")
                            .header("Authorization", "Bearer " + staff)
                            .contentType("application/json")
                            .content(json.writeValueAsString(
                                    Map.of("counselorNo", "C2026001", "name", "辅导员甲", "password", "Password@123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        Long counselorId = jdbc.queryForObject("SELECT id FROM counselor WHERE counselor_no='C2026001'", Long.class);
        var account = service.login(credentials("C2026001", "STAFF", "Password@123")).getUserMsgVO();
        assertEquals(counselorId, account.getCounselorId());
        assertEquals("辅导员甲", account.getRealName());
        assertEquals("BCRYPT", jdbc.queryForObject("SELECT password_algorithm FROM sys_user WHERE id=?", String.class,
                                                   account.getId()));
    }

    @Test
    void counselorAccountUsesDefaultPasswordWhenOmitted() throws Exception
    {
        String staff = staffToken();
        mvc.perform(post("/counselor/add-counselor")
                            .header("Authorization", "Bearer " + staff)
                            .contentType("application/json")
                            .content(json.writeValueAsString(
                                    Map.of("counselorNo", "C-default", "name", "默认密码老师"))))
                .andExpect(jsonPath("$.code").value(200));
        var login = service.login(credentials("C-default", "STAFF", "123456"));
        assertNotNull(login.getUserMsgVO().getCounselorId());
        String hash = jdbc.queryForObject("SELECT password FROM sys_user WHERE username='C-default'", String.class);
        assertNotEquals("123456", hash);
        assertTrue(new BCryptPasswordEncoder().matches("123456", hash));
    }

    @Test
    void counselorCreationRollsBackWhenAccountRegistrationFails() throws Exception
    {
        String staff = staffToken();
        mvc.perform(post("/counselor/add-counselor")
                            .header("Authorization", "Bearer " + staff)
                            .contentType("application/json")
                            .content(json.writeValueAsString(Map.of("counselorNo", "C-conflict", "name", "辅导员乙",
                                                                    "username", "staff", "password", "Password@123"))))
                .andExpect(jsonPath("$.code").value(600));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM counselor", Integer.class));
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM sys_user", Integer.class));
        mvc.perform(post("/counselor/add-counselor")
                            .header("Authorization", "Bearer " + staff)
                            .contentType("application/json")
                            .content(json.writeValueAsString(
                                    Map.of("counselorNo", "C-invalid", "name", "辅导员乙", "password", "123"))))
                .andExpect(jsonPath("$.code").value(600));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM counselor", Integer.class));
    }

    @Test
    void studentRegistrationCreatesBothRowsAndLoginReturnsSeparateIds()
    {
        staffToken();
        var created = service.register(registration("20260001", "STUDENT"));
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM sys_user_role WHERE user_id=?", Integer.class,
                                            created.getId()));
        assertEquals(
                "STUDENT",
                jdbc.queryForObject(
                        "SELECT r.role_code FROM sys_user_role ur JOIN sys_role r ON r.id=ur.role_id WHERE ur.user_id=?",
                        String.class, created.getId()));
        assertNotNull(created.getStudentUuid());
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM student", Integer.class));
        var login = service.login(credentials("20260001", "STUDENT", "Password@123"));
        assertEquals(created.getId(), TokenUserDTO.parseAccountId(login.getToken()));
        assertEquals(created.getStudentUuid(), TokenUserDTO.parseUuid(login.getToken()));
        assertNotEquals(created.getId(), created.getStudentUuid());
        assertEquals("测试学院", login.getStudentMsgVO().getCollege());
        assertThrows(RuntimeException.class, () -> service.login(credentials("20260001", "STAFF", "Password@123")));
    }
    @Test
    void missingOrDisabledStudentRoleRollsBackRegistration()
    {
        jdbc.update("UPDATE sys_role SET status=0 WHERE role_code='STUDENT'");
        assertThrows(RuntimeException.class, () -> service.register(registration("disabled-role", "STUDENT")));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM student", Integer.class));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM sys_user", Integer.class));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM sys_user_role", Integer.class));
        jdbc.update("DELETE FROM sys_role");
        assertThrows(RuntimeException.class, () -> service.register(registration("missing-role", "STUDENT")));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM student", Integer.class));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM sys_user", Integer.class));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM sys_user_role", Integer.class));
    }

    @Test
    void failedAccountInsertRollsBackInsertedStudent()
    {
        var dto = registration("rollback", "STUDENT");
        dto.getStudent().setName("模拟数据库拒绝");
        assertThrows(RuntimeException.class, () -> service.register(dto));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM student", Integer.class));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM sys_user", Integer.class));
    }
    @Test
    void legacyPasswordUpgradesWithoutChangingPassword()
    {
        var created = service.register(registration("legacy", "STUDENT"));
        jdbc.update("UPDATE sys_user SET password=?,password_algorithm='MD5' WHERE id=?",
                    StringTools.encodeByMD5("old123"), created.getId());
        assertThrows(RuntimeException.class, () -> service.login(credentials("legacy", "STUDENT", "wrong")));
        assertEquals("MD5", jdbc.queryForObject("SELECT password_algorithm FROM sys_user WHERE id=?", String.class,
                                                created.getId()));
        service.login(credentials("legacy", "STUDENT", "old123"));
        assertEquals("BCRYPT", jdbc.queryForObject("SELECT password_algorithm FROM sys_user WHERE id=?", String.class,
                                                   created.getId()));
        assertTrue(new BCryptPasswordEncoder().matches(
                "old123",
                jdbc.queryForObject("SELECT password FROM sys_user WHERE id=?", String.class, created.getId())));
        service.login(credentials("legacy", "STUDENT", "old123"));
    }
    @Test
    void unifiedControllerProtectsStudentAndStaffBoundaries() throws Exception
    {
        String staff = staffToken();
        mvc.perform(post("/sys-user/register")
                            .contentType("application/json")
                            .content(json.writeValueAsString(registration("20260002", "STUDENT"))))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/sys-user/register")
                            .header("Authorization", "Bearer " + staff)
                            .contentType("application/json")
                            .content(json.writeValueAsString(registration("20260002", "STUDENT"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        var login = service.login(credentials("20260002", "STUDENT", "Password@123"));
        String student = login.getToken();
        mvc.perform(post("/sys-user/register")
                            .header("Authorization", "Bearer " + student)
                            .contentType("application/json")
                            .content("{}"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/sys-user/updateMsg")
                            .header("Authorization", "Bearer " + staff)
                            .contentType("application/json")
                            .content("{}"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/sys-user/updateMsg")
                            .header("Authorization", "Bearer " + student)
                            .contentType("application/json")
                            .content("{\"phone\":\"13800138000\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        assertEquals("13800138000", jdbc.queryForObject("SELECT phone FROM student WHERE uuid=?", String.class,
                                                        login.getUserMsgVO().getStudentUuid()));
        mvc.perform(put("/sys-user/updateInfo")
                            .header("Authorization", "Bearer " + student)
                            .contentType("application/json")
                            .content("{\"username\":\"changed\"}"))
                .andExpect(jsonPath("$.code").value(600));
        service.softDelete(login.getUserMsgVO().getId());
        mvc.perform(post("/sys-user/updateMsg")
                            .header("Authorization", "Bearer " + student)
                            .contentType("application/json")
                            .content("{}"))
                .andExpect(status().isUnauthorized());
    }
    @Test
    void loginResponseNeverExposesPasswordsAndOldRoutesAreClosed() throws Exception
    {
        staffToken();
        mvc.perform(
                   post("/sys-user/login")
                           .contentType("application/json")
                           .content("{\"username\":\"staff\",\"password\":\"Password@123\",\"accountType\":\"STAFF\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userMsgVO.accountType").value("STAFF"))
                .andExpect(jsonPath("$.data.userMsgVO.password").doesNotExist());
        mvc.perform(post("/user/login").contentType("application/json").content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void filterRejectsMissingEmptyMalformedAndTamperedCredentials() throws Exception
    {
        String valid = staffToken();
        String[] parts = valid.split("\\.");
        String replacement = Base64.getUrlEncoder().withoutPadding().encodeToString(
                "{\"sysUserId\":999,\"accountType\":\"STAFF\"}".getBytes(StandardCharsets.UTF_8));
        for (String header : new String[]
        {"Bearer ", "Basic abc", "Bearer not-a-token",
                                           "Bearer " + parts[0] + "." + replacement + "." + parts[2],
                                           "Bearer " + parts[0] + "." + parts[1] + "."
                                           })
        {
            mvc.perform(put("/sys-user/updateInfo")
                                .header("Authorization", header)
                                .contentType("application/json")
                                .content("{\"realName\":\"不得写入\"}"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value(401));
        }
        mvc.perform(put("/sys-user/updateInfo").contentType("application/json").content("{}"))
                .andExpect(status().isUnauthorized());
        assertEquals("测试老师",
                     jdbc.queryForObject("SELECT real_name FROM sys_user WHERE username='staff'", String.class));
    }
    @Test
    void filterRejectsDeletedOrRenamedAccount() throws Exception
    {
        String token = staffToken();
        jdbc.update("UPDATE sys_user SET username='renamed' WHERE username='staff'");
        mvc.perform(put("/sys-user/updateInfo")
                            .header("Authorization", "Bearer " + token)
                            .contentType("application/json")
                            .content("{}"))
                .andExpect(status().isUnauthorized());
        jdbc.update("DELETE FROM sys_user");
        mvc.perform(put("/sys-user/updateInfo")
                            .header("Authorization", "Bearer " + token)
                            .contentType("application/json")
                            .content("{}"))
                .andExpect(status().isUnauthorized());
    }
    @Test
    void filterRejectsChangedStudentBinding() throws Exception
    {
        var first = service.register(registration("student-a", "STUDENT"));
        var second = service.register(registration("student-b", "STUDENT"));
        String token = service.login(credentials("student-a", "STUDENT", "Password@123")).getToken();
        jdbc.update("DELETE FROM sys_user WHERE id=?", second.getId());
        jdbc.update("UPDATE sys_user SET student_uuid=? WHERE id=?", second.getStudentUuid(), first.getId());
        mvc.perform(post("/sys-user/updateMsg")
                            .header("Authorization", "Bearer " + token)
                            .contentType("application/json")
                            .content("{\"phone\":\"13800138000\"}"))
                .andExpect(status().isUnauthorized());
        assertNull(
                jdbc.queryForObject("SELECT phone FROM student WHERE uuid=?", String.class, second.getStudentUuid()));
    }
}
