package com.wzbc.EmploymentServer;

import com.wzbc.EmploymentServer.Mapper.BasicKnowledgeMapper;
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

@SpringBootTest(properties =
{"spring.config.import=",
                              "spring.datasource.url=jdbc:h2:mem:knowledge;MODE=MySQL;DB_CLOSE_DELAY=-1",
                              "spring.datasource.username=sa", "spring.datasource.password=",
                              "spring.datasource.driver-class-name=org.h2.Driver", "spring.sql.init.mode=always",
                              "spring.sql.init.schema-locations=classpath:account-schema.sql"
                              })
@AutoConfigureMockMvc
class BasicKnowledgeTests
{
    @Autowired
    MockMvc mvc;
    @Autowired
    JdbcTemplate jdbc;
    @Autowired
    BasicKnowledgeMapper mapper;
    private final String auth = "Bearer " + TokenUserDTO.generateSysUserToken("admin", 10L);
    @BeforeEach
    void setup()
    {
        jdbc.execute(
                "CREATE TABLE IF NOT EXISTS basic_knowledge(id BIGINT AUTO_INCREMENT PRIMARY KEY,title VARCHAR(200),answer TEXT,status INT)");
        jdbc.update("DELETE FROM basic_knowledge");
        jdbc.update("DELETE FROM sys_user");
        jdbc.update("INSERT INTO sys_user(id,username,password,account_type) VALUES(10,'admin','unused','STAFF')");
    }
    @Test
    void crudAndEnabledQuery() throws Exception
    {
        mvc.perform(post("/basic-knowledge/add")
                            .header("Authorization", auth)
                            .contentType("application/json")
                            .content("{\"title\":\"备案问题\",\"answer\":\"请准备材料\",\"status\":0}"))
                .andExpect(jsonPath("$.code").value(200));
        Long id = jdbc.queryForObject("SELECT id FROM basic_knowledge", Long.class);
        assertTrue(mapper.selectAllEnabled().isEmpty());
        mvc.perform(put("/basic-knowledge/update")
                            .header("Authorization", auth)
                            .contentType("application/json")
                            .content("{\"id\":" + id + ",\"title\":\"备案问题\",\"answer\":\"完整回答\",\"status\":1}"))
                .andExpect(jsonPath("$.code").value(200));
        assertEquals(1, mapper.selectAllEnabled().size());
        assertEquals("完整回答", mapper.selectAllEnabled().getFirst().getAnswer());
        mvc.perform(get("/basic-knowledge/" + id).header("Authorization", auth))
                .andExpect(jsonPath("$.data.status").value(1));
        mvc.perform(get("/basic-knowledge/page").header("Authorization", auth))
                .andExpect(jsonPath("$.data.totalCount").value(1));
        jdbc.update("INSERT INTO basic_knowledge(title,answer,status) VALUES('禁用','不可返回',0)");
        assertEquals(1, mapper.selectAllEnabled().size());
        mvc.perform(delete("/basic-knowledge/" + id).header("Authorization", auth))
                .andExpect(jsonPath("$.code").value(200));
        assertTrue(mapper.selectAllEnabled().isEmpty());
    }
    @Test
    void rejectsInvalidInputAndAnonymous() throws Exception
    {
        mvc.perform(post("/basic-knowledge/add")
                            .header("Authorization", auth)
                            .contentType("application/json")
                            .content("{\"title\":\" \",\"answer\":\"答案\",\"status\":2}"))
                .andExpect(jsonPath("$.code").value(600));
        mvc.perform(get("/basic-knowledge/page")).andExpect(status().isUnauthorized());
        mvc.perform(get("/basic-knowledge/page").param("pageNum", "0").header("Authorization", auth))
                .andExpect(jsonPath("$.code").value(600));
    }
}
