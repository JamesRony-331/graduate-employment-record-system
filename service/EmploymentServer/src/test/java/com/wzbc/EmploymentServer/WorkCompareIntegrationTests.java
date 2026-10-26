package com.wzbc.EmploymentServer;

import com.wzbc.EmploymentServer.entity.DTO.Token.TokenUserDTO;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import static org.hamcrest.Matchers.lessThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(
        properties =
                {"spring.config.import=", "spring.datasource.url=jdbc:h2:mem:work-compare;MODE=MySQL;DB_CLOSE_DELAY=-1",
                 "spring.datasource.username=sa", "spring.datasource.password=",
                 "spring.datasource.driver-class-name=org.h2.Driver", "spring.sql.init.mode=always",
                 "spring.sql.init.schema-locations=classpath:account-schema.sql,classpath:work-compare-schema.sql"
                 })
@AutoConfigureMockMvc
class WorkCompareIntegrationTests
{
    @Autowired
    MockMvc mvc;
    @Autowired
    JdbcTemplate jdbc;
    private final String auth = "Bearer " + TokenUserDTO.generateSysUserToken("admin", 10L);
    private final String body = """
        {"companyName":"测试公司","companyContact":"jobs@example.com","education":"本科","experience":0,"skills":["Java","MySQL"],"categories":["后端开发"],"cities":["杭州"],"jobType":"全职","priority":"技能匹配","minimumSalary":5000,"expectedSalary":8000,"strict":{"cities":true}}
        """;
    @BeforeEach
    void setup()
    {
        jdbc.execute(
                "CREATE TABLE IF NOT EXISTS work_skill(id BIGINT AUTO_INCREMENT PRIMARY KEY,name VARCHAR(100) UNIQUE NOT NULL)");
        jdbc.update("DELETE FROM work_skill");
        jdbc.update("INSERT INTO work_skill(name) VALUES('Java'),('MySQL')");
        jdbc.update("DELETE FROM work_compare");
        jdbc.update("DELETE FROM sys_user");
        jdbc.update("INSERT INTO sys_user(id,username,password,account_type) VALUES(10,'admin','unused','STAFF')");
    }
    @Test
    void crudRoundTripsArraysNullsAndSwitches() throws Exception
    {
        mvc.perform(post("/work-compare/add")
                            .header("Authorization", auth)
                            .contentType("application/json")
                            .content(body))
                .andExpect(jsonPath("$.code").value(200));
        Long id = jdbc.queryForObject("SELECT id FROM work_compare", Long.class);
        mvc.perform(get("/work-compare/" + id).header("Authorization", auth))
                .andExpect(jsonPath("$.data.companyName").value("测试公司"))
                .andExpect(jsonPath("$.data.companyContact").value("jobs@example.com"))
                .andExpect(jsonPath("$.data.skills[1]").value("MySQL"))
                .andExpect(jsonPath("$.data.strict.cities").value(true))
                .andExpect(jsonPath("$.data.major").doesNotExist());
        mvc.perform(get("/work-compare/page").header("Authorization", auth))
                .andExpect(jsonPath("$.data.totalCount").value(1))
                .andExpect(jsonPath("$.data.records[0].experience").value(0));
        mvc.perform(put("/work-compare/" + id)
                            .header("Authorization", auth)
                            .contentType("application/json")
                            .content(body.replace("杭州", "上海").replace("8000", "9000")))
                .andExpect(jsonPath("$.code").value(200));
        mvc.perform(get("/work-compare/" + id).header("Authorization", auth))
                .andExpect(jsonPath("$.data.cities[0]").value("上海"));
        mvc.perform(delete("/work-compare/" + id).header("Authorization", auth))
                .andExpect(jsonPath("$.code").value(200));
        mvc.perform(get("/work-compare/" + id).header("Authorization", auth)).andExpect(jsonPath("$.code").value(404));
    }
    @Test
    void skillsOptionsAndUnknownSkills() throws Exception
    {
        for (int i = 0; i < 25; i++)
            jdbc.update("INSERT INTO work_skill(name) VALUES(?)", "Skill" + i);
        mvc.perform(get("/work-skill/options").header("Authorization", auth))
                .andExpect(jsonPath("$.data.length()").value(20));
        mvc.perform(get("/work-skill/options").param("keyword", "Skill24").header("Authorization", auth))
                .andExpect(jsonPath("$.data[0].name").value("Skill24"));
        mvc.perform(post("/work-compare/add")
                            .header("Authorization", auth)
                            .contentType("application/json")
                            .content(body.replace("Java", "UnknownSkill")))
                .andExpect(jsonPath("$.code").value(600));
        mvc.perform(post("/work-skill/add")
                            .header("Authorization", auth)
                            .contentType("application/json")
                            .content("{\"name\":\"Rust\"}"))
                .andExpect(jsonPath("$.code").value(200));
        mvc.perform(get("/work-skill/page").param("keyword", "Rust").header("Authorization", auth))
                .andExpect(jsonPath("$.data.totalCount").value(1));
    }
    @Test
    void recommendationsReturnJobDetailsAndDescendingSimilarity() throws Exception
    {
        jdbc.update("DELETE FROM student");
        jdbc.update("INSERT INTO student(uuid,student_no,name) VALUES(20,'S20','测试学生')");
        jdbc.update(
                "INSERT INTO sys_user(id,username,password,account_type,student_uuid) VALUES(20,'student20','unused','STUDENT',20)");
        String studentAuth = "Bearer " + TokenUserDTO.generateAccountToken("student20", 20L, "STUDENT", 20L);
        mvc.perform(post("/work-select")
                            .header("Authorization", studentAuth)
                            .contentType("application/json")
                            .content(body))
                .andExpect(jsonPath("$.data.length()").value(0));
        mvc.perform(post("/work-compare/add")
                            .header("Authorization", auth)
                            .contentType("application/json")
                            .content(body))
                .andExpect(jsonPath("$.code").value(200));
        Long bestId = jdbc.queryForObject("SELECT id FROM work_compare", Long.class);
        mvc.perform(post("/work-compare/add")
                            .header("Authorization", auth)
                            .contentType("application/json")
                            .content(body.replace("本科", "硕士")))
                .andExpect(jsonPath("$.code").value(200));
        mvc.perform(post("/work-select")
                            .header("Authorization", studentAuth)
                            .contentType("application/json")
                            .content(body))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].id").value(bestId))
                .andExpect(jsonPath("$.data[0].similarity").value(100.0))
                .andExpect(jsonPath("$.data[1].similarity").value(lessThan(100.0)))
                .andExpect(jsonPath("$.data[0].skills[1]").value("MySQL"))
                .andExpect(jsonPath("$.data[0].strict.cities").value(true));
    }
    @Test
    void invalidRequestsAreRejected() throws Exception
    {
        mvc.perform(post("/work-compare/add")
                            .header("Authorization", auth)
                            .contentType("application/json")
                            .content(body.replace("8000", "1000")))
                .andExpect(jsonPath("$.code").value(600));
        mvc.perform(post("/work-compare/add")
                            .header("Authorization", auth)
                            .contentType("application/json")
                            .content(body.replace("杭州", "不限")))
                .andExpect(jsonPath("$.code").value(600));
        mvc.perform(get("/work-compare/page").param("pageNum", "0").header("Authorization", auth))
                .andExpect(jsonPath("$.code").value(600));
        mvc.perform(get("/work-compare/page")).andExpect(status().isUnauthorized());
    }
}
