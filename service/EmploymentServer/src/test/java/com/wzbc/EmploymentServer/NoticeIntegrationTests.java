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

@SpringBootTest(properties =
{"spring.config.import=",
                              "spring.datasource.url=jdbc:h2:mem:notice-tests;MODE=MySQL;DB_CLOSE_DELAY=-1",
                              "spring.datasource.username=sa", "spring.datasource.password=",
                              "spring.datasource.driver-class-name=org.h2.Driver", "spring.sql.init.mode=always",
                              "spring.sql.init.schema-locations=classpath:account-schema.sql"
                              })
@AutoConfigureMockMvc
class NoticeIntegrationTests
{
    @Autowired
    MockMvc mvc;
    @Autowired
    JdbcTemplate jdbc;
    String token = TokenUserDTO.generateSysUserToken("teacher", 10L);
    @BeforeEach
    void setup()
    {
        jdbc.execute(
                "CREATE TABLE IF NOT EXISTS sys_notice(id BIGINT AUTO_INCREMENT PRIMARY KEY,title VARCHAR(200) NOT NULL,content CLOB NOT NULL,notice_type INT NOT NULL,status INT NOT NULL,is_top INT NOT NULL,publisher_id BIGINT NOT NULL,publisher_name VARCHAR(100),publish_time TIMESTAMP(3),created_at TIMESTAMP(3) NOT NULL,updated_at TIMESTAMP(3) NOT NULL)");
        jdbc.update("DELETE FROM sys_notice");
        jdbc.update("DELETE FROM sys_user");
        jdbc.update(
                "INSERT INTO sys_user(id,username,password,account_type,real_name) VALUES(10,'teacher','unused','STAFF','老师'),(11,'other','unused','STAFF','其他老师')");
    }
    @Test
    void crudPublishAndWithdrawPreservePublisher() throws Exception
    {
        mvc.perform(post("/notice/add")
                            .header("Authorization", "Bearer " + token)
                            .contentType("application/json")
                            .content("{\"title\":\"就业公告\",\"content\":\"<p>正文</p>\",\"noticeType\":3}"))
                .andExpect(jsonPath("$.code").value(200));
        Long id = jdbc.queryForObject("SELECT id FROM sys_notice", Long.class);
        mvc.perform(get("/notice/" + id).header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.data.publisherId").value(10))
                .andExpect(jsonPath("$.data.publisherName").value("老师"))
                .andExpect(jsonPath("$.data.status").value(0))
                .andExpect(jsonPath("$.data.publishTime").isEmpty());
        mvc.perform(put("/notice/update")
                            .header("Authorization", "Bearer " + token)
                            .contentType("application/json")
                            .content("{\"id\":" + id + ",\"status\":1,\"isTop\":1}"))
                .andExpect(jsonPath("$.code").value(200));
        var published = jdbc.queryForObject("SELECT publish_time FROM sys_notice", Timestamp.class);
        assertNotNull(published);
        mvc.perform(put("/notice/update")
                            .header("Authorization", "Bearer " + token)
                            .contentType("application/json")
                            .content("{\"id\":" + id + ",\"status\":2}"))
                .andExpect(jsonPath("$.code").value(200));
        assertEquals(published, jdbc.queryForObject("SELECT publish_time FROM sys_notice", Timestamp.class));
        mvc.perform(get("/notice/page").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.data.totalCount").value(1))
                .andExpect(jsonPath("$.data.records[0].isTop").value(1));
        String other = TokenUserDTO.generateSysUserToken("other", 11L);
        mvc.perform(delete("/notice/" + id).header("Authorization", "Bearer " + other))
                .andExpect(jsonPath("$.code").value(403));
        mvc.perform(delete("/notice/" + id).header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.code").value(200));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM sys_notice", Integer.class));
    }
    @Test
    void invalidInputAndAnonymousRequestsAreRejected() throws Exception
    {
        mvc.perform(post("/notice/add")
                            .header("Authorization", "Bearer " + token)
                            .contentType("application/json")
                            .content("{\"title\":\" \" ,\"content\":\"正文\",\"noticeType\":9}"))
                .andExpect(jsonPath("$.code").value(600));
        mvc.perform(get("/notice/page").param("pageNum", "0").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.code").value(600));
        mvc.perform(get("/notice/page")).andExpect(status().isUnauthorized());
        mvc.perform(get("/notice/999").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.code").value(600));
    }
    @Test
    void studentSeesOnlyPublishedAndCannotManageNotices() throws Exception
    {
        jdbc.update("DELETE FROM student WHERE uuid=100");
        jdbc.update("INSERT INTO student(uuid,student_no,name) VALUES(100,'S100','学生')");
        jdbc.update(
                "INSERT INTO sys_user(id,username,password,account_type,student_uuid) VALUES(100,'S100','unused','STUDENT',100)");
        String student = TokenUserDTO.generateAccountToken("S100", 100L, "STUDENT", 100L);
        for (int id = 1; id <= 4; id++)
            jdbc.update(
                    "INSERT INTO sys_notice(id,title,content,notice_type,status,is_top,publisher_id,created_at,updated_at,publish_time) VALUES(?,?,'正文',1,?,?,10,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)",
                    id, "公告" + id,
                    id == 1   ? 0
                    : id == 2 ? 2
                              : 1,
                    id == 3 ? 1 : 0);
        mvc.perform(get("/notice/published/page").param("pageSize", "1").header("Authorization", "Bearer " + student))
                .andExpect(jsonPath("$.data.totalCount").value(2))
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.records[0].id").value(3));
        mvc.perform(get("/notice/published/page")
                            .param("pageSize", "1")
                            .param("pageNum", "2")
                            .header("Authorization", "Bearer " + student))
                .andExpect(jsonPath("$.data.records[0].id").value(4));
        for (int id : new int[]
        {1, 2, 99
        })
            mvc.perform(get("/notice/published/" + id).header("Authorization", "Bearer " + student))
                    .andExpect(jsonPath("$.code").value(404));
        mvc.perform(get("/notice/published/3").header("Authorization", "Bearer " + student))
                .andExpect(jsonPath("$.data.content").value("正文"));
        jdbc.update("UPDATE sys_notice SET status=2 WHERE id=3");
        mvc.perform(get("/notice/published/3").header("Authorization", "Bearer " + student))
                .andExpect(jsonPath("$.code").value(404));
        for (String path : new String[]
        {"/notice/page", "/notice/1"
        })
            mvc.perform(get(path).header("Authorization", "Bearer " + student)).andExpect(status().isForbidden());
        mvc.perform(post("/notice/add")
                            .header("Authorization", "Bearer " + student)
                            .contentType("application/json")
                            .content("{}"))
                .andExpect(status().isForbidden());
        mvc.perform(put("/notice/update")
                            .header("Authorization", "Bearer " + student)
                            .contentType("application/json")
                            .content("{}"))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/notice/3").header("Authorization", "Bearer " + student)).andExpect(status().isForbidden());
        mvc.perform(get("/notice/published/page")).andExpect(status().isUnauthorized());
        mvc.perform(get("/notice/published/page").param("pageSize", "101").header("Authorization", "Bearer " + student))
                .andExpect(jsonPath("$.code").value(600));
    }
}
