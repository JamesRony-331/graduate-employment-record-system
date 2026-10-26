package com.wzbc.EmploymentServer;

import java.sql.Timestamp;

import com.wzbc.EmploymentServer.entity.DTO.Token.TokenUserDTO;
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
                {"spring.config.import=", "spring.datasource.url=jdbc:h2:mem:major-crud;MODE=MySQL;DB_CLOSE_DELAY=-1",
                 "spring.datasource.username=sa", "spring.datasource.password=",
                 "spring.datasource.driver-class-name=org.h2.Driver", "spring.sql.init.mode=always",
                 "spring.sql.init.schema-locations=classpath:account-schema.sql,classpath:counselor-class-schema.sql,classpath:department-admin-schema.sql"
                 })
@AutoConfigureMockMvc
class MajorCrudIntegrationTests
{
    @Autowired
    MockMvc mvc;
    @Autowired
    JdbcTemplate jdbc;
    private final String token = TokenUserDTO.generateSysUserToken("admin", 10L);
    @BeforeEach
    void setup()
    {
        jdbc.update("DELETE FROM class_info");
        jdbc.update("DELETE FROM sys_user");
        jdbc.update("DELETE FROM student");
        jdbc.update("DELETE FROM college");
        jdbc.execute("DROP TABLE IF EXISTS major");
        jdbc.execute(
                "CREATE TABLE major(id BIGINT AUTO_INCREMENT PRIMARY KEY,college_id BIGINT NOT NULL,major_name VARCHAR(100) NOT NULL,created_at TIMESTAMP NOT NULL,updated_at TIMESTAMP NOT NULL,UNIQUE(college_id,major_name))");
        jdbc.update("INSERT INTO college(id,college_code,college_name) VALUES(1,'A','学院一'),(2,'B','学院二')");
        jdbc.update("INSERT INTO sys_user(id,username,password,account_type) VALUES(10,'admin','unused','STAFF')");
    }
    @Test
    void crudAndPaginationPreserveExistingOptions() throws Exception
    {
        mvc.perform(post("/major/add")
                            .header("Authorization", "Bearer " + token)
                            .contentType("application/json")
                            .content("{\"collegeId\":1,\"majorName\":\"软件工程\"}"))
                .andExpect(jsonPath("$.code").value(200));
        Long id = jdbc.queryForObject("SELECT id FROM major", Long.class);
        assertNotNull(jdbc.queryForObject("SELECT created_at FROM major", Timestamp.class));
        mvc.perform(get("/major/" + id).header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.data.majorName").value("软件工程"));
        mvc.perform(get("/major/page").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.data.totalCount").value(1));
        mvc.perform(get("/major/options").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.data.length()").value(1));
        mvc.perform(put("/major/update")
                            .header("Authorization", "Bearer " + token)
                            .contentType("application/json")
                            .content("{\"id\":" + id + ",\"majorName\":\"计算机科学\",\"collegeId\":2}"))
                .andExpect(jsonPath("$.code").value(200));
        assertEquals(2L, jdbc.queryForObject("SELECT college_id FROM major", Long.class));
        assertEquals("计算机科学", jdbc.queryForObject("SELECT major_name FROM major", String.class));
        mvc.perform(delete("/major/" + id).header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.code").value(200));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM major", Integer.class));
    }
    @Test
    void referencesAndInvalidRequestsAreRejected() throws Exception
    {
        jdbc.update(
                "INSERT INTO major(id,college_id,major_name,created_at,updated_at) VALUES(1,1,'专业',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)");
        jdbc.update(
                "INSERT INTO class_info(id,class_code,class_name,college_id,major_id,student_count) VALUES(1,'C','班级',1,1,0)");
        mvc.perform(delete("/major/1").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.code").value(600));
        mvc.perform(put("/major/update")
                            .header("Authorization", "Bearer " + token)
                            .contentType("application/json")
                            .content("{\"id\":1,\"collegeId\":2}"))
                .andExpect(jsonPath("$.code").value(600));
        mvc.perform(post("/major/add")
                            .header("Authorization", "Bearer " + token)
                            .contentType("application/json")
                            .content("{\"collegeId\":999,\"majorName\":\"专业\"}"))
                .andExpect(jsonPath("$.code").value(600));
        mvc.perform(post("/major/add")
                            .header("Authorization", "Bearer " + token)
                            .contentType("application/json")
                            .content("{\"collegeId\":1,\"majorName\":\" \"}"))
                .andExpect(jsonPath("$.code").value(600));
        mvc.perform(get("/major/page").param("pageNum", "0").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.code").value(600));
        mvc.perform(get("/major/page")).andExpect(status().isUnauthorized());
    }
}
