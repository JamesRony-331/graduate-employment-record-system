package com.wzbc.EmploymentServer;

import java.sql.Timestamp;
import org.hamcrest.Matchers;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.wzbc.EmploymentServer.entity.DTO.Token.TokenUserDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(
        properties =
                {"spring.config.import=",
                 "spring.datasource.url=jdbc:h2:mem:managed-class-tests;MODE=MySQL;DB_CLOSE_DELAY=-1",
                 "spring.datasource.username=sa", "spring.datasource.password=",
                 "spring.datasource.driver-class-name=org.h2.Driver", "spring.sql.init.mode=always",
                 "spring.sql.init.schema-locations=classpath:account-schema.sql,classpath:counselor-class-schema.sql,classpath:department-admin-schema.sql"
                 })
@AutoConfigureMockMvc
class ManagedClassIntegrationTests
{
    @Autowired
    JdbcTemplate jdbc;
    @Autowired
    MockMvc mvc;
    private final String token = TokenUserDTO.generateSysUserToken("teacher", 10L);

    @BeforeEach
    void setup()
    {
        jdbc.execute(
                "CREATE TABLE IF NOT EXISTS employment_audit_record(id BIGINT AUTO_INCREMENT PRIMARY KEY,filing_id BIGINT,audit_stage INT,auditor_id BIGINT,auditor_name VARCHAR(64),auditor_role INT,audit_result INT,audit_opinion VARCHAR(1000),from_status INT,to_status INT,audited_at TIMESTAMP,UNIQUE(filing_id,audit_stage))");
        jdbc.update("DELETE FROM employment_audit_record");
        jdbc.update("DELETE FROM employment_filing");
        jdbc.update("DELETE FROM college_admin");
        jdbc.update("DELETE FROM counselor_class");
        jdbc.update("DELETE FROM sys_user_role");
        jdbc.update("DELETE FROM sys_role");
        jdbc.update("DELETE FROM sys_user");
        jdbc.update("DELETE FROM student");
        jdbc.update("DELETE FROM counselor");
        jdbc.update("DELETE FROM class_info");
        jdbc.update("DELETE FROM college");
        jdbc.update("INSERT INTO counselor(id,counselor_no,name,status) VALUES(1,'C1','老师',1)");
        jdbc.update(
                "INSERT INTO sys_user(id,username,password,account_type,counselor_id) VALUES(10,'teacher','unused','STAFF',1)");
        jdbc.update(
                "INSERT INTO sys_role(id,role_code,role_name,status) VALUES(1,'COUNSELOR','辅导员',1),(2,'DEPARTMENT_HEAD','院系管理员',1),(3,'STUDENT','学生',1)");
        jdbc.update("INSERT INTO college(id,college_code,college_name) VALUES(1,'A','学院一'),(2,'B','学院二')");
        jdbc.update(
                "INSERT INTO class_info(id,class_code,class_name,college_id,student_count) VALUES(1,'A1','班级一',1,10),(2,'A2','班级二',1,20),(3,'B1','其他学院',2,30)");
        jdbc.update("INSERT INTO counselor_class(counselor_id,class_id,role_type) VALUES(1,1,1),(1,3,2)");
        jdbc.update(
                "INSERT INTO college_admin(user_id,name,college_id,created_at,updated_at) VALUES(10,'老师',1,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)");
    }
    void bind(int id)
    {
        jdbc.update("INSERT INTO sys_user_role(user_id,role_id) VALUES(10,?)", id);
    }

    @Test
    void studentManagementCreatesBothRowsAndUpdatesNameAtomically() throws Exception
    {
        bind(1);
        jdbc.update("UPDATE class_info SET major_id=1 WHERE id=1");
        String body = "{\"studentNo\":\"SNEW\",\"name\":\"新同学\",\"idCard\":\"330302200001011234\",\"classId\":1}";
        mvc.perform(post("/student/add")
                            .header("Authorization", "Bearer " + token)
                            .contentType("application/json")
                            .content(body))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("新同学"));
        Long uuid = jdbc.queryForObject("SELECT uuid FROM student WHERE student_no='SNEW'", Long.class);
        assertEquals(uuid, jdbc.queryForObject("SELECT student_uuid FROM sys_user WHERE username='SNEW'", Long.class));
        assertEquals(
                1,
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM sys_user_role ur JOIN sys_user u ON u.id=ur.user_id WHERE u.username='SNEW' AND ur.role_id=3",
                        Integer.class));
        assertTrue(new BCryptPasswordEncoder().matches(
                "011234", jdbc.queryForObject("SELECT password FROM sys_user WHERE username='SNEW'", String.class)));
        mvc.perform(put("/student/update")
                            .header("Authorization", "Bearer " + token)
                            .contentType("application/json")
                            .content("{\"uuid\":" + uuid + ",\"name\":\"新姓名\",\"phone\":\"13800138000\"}"))
                .andExpect(jsonPath("$.code").value(200));
        assertEquals("新姓名",
                     jdbc.queryForObject("SELECT real_name FROM sys_user WHERE username='SNEW'", String.class));
        mvc.perform(get("/student/" + uuid).header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.data.name").value("新姓名"))
                .andExpect(jsonPath("$.data.phone").value("13800138000"));
        mvc.perform(put("/student/update")
                            .header("Authorization", "Bearer " + token)
                            .contentType("application/json")
                            .content("{\"uuid\":" + uuid + ",\"classId\":2,\"name\":\"越权\"}"))
                .andExpect(jsonPath("$.code").value(403));
        assertEquals("新姓名", jdbc.queryForObject("SELECT name FROM student WHERE uuid=?", String.class, uuid));
        jdbc.update("UPDATE sys_role SET status=0 WHERE id=3");
        mvc.perform(post("/student/add")
                            .header("Authorization", "Bearer " + token)
                            .contentType("application/json")
                            .content(body.replace("SNEW", "ROLLBACK")))
                .andExpect(jsonPath("$.code").value(600));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM student WHERE student_no='ROLLBACK'", Integer.class));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM sys_user WHERE username='ROLLBACK'", Integer.class));
    }

    @Test
    void studentUpdateRejectsMalformedContactAndIdentityBeforeWriting() throws Exception
    {
        bind(1);
        for (String fragment :
             new String[]
             {"\"phone\":\"bad\"", "\"email\":\"bad\"", "\"idCard\":\"bad\"", "\"enrollmentYear\":1800"
             })
        {
            mvc.perform(put("/student/update")
                                .header("Authorization", "Bearer " + token)
                                .contentType("application/json")
                                .content("{\"uuid\":1," + fragment + "}"))
                    .andExpect(jsonPath("$.code").value(600));
        }
    }

    @Test
    void studentInitialPasswordUsesLastSixCharactersIncludingX() throws Exception
    {
        bind(1);
        jdbc.update("UPDATE class_info SET major_id=1 WHERE id=1");
        mvc.perform(
                   post("/student/add")
                           .header("Authorization", "Bearer " + token)
                           .contentType("application/json")
                           .content(
                                   "{\"studentNo\":\"SX\",\"name\":\"测试学生\",\"idCard\":\"33030220000101123X\",\"classId\":1,\"password\":\"ignored\"}"))
                .andExpect(jsonPath("$.code").value(200));
        String hash = jdbc.queryForObject("SELECT password FROM sys_user WHERE username='SX'", String.class);
        var encoder = new BCryptPasswordEncoder();
        assertTrue(encoder.matches("01123X", hash));
        assertFalse(encoder.matches("ignored", hash));
    }

    @Test
    void studentPaginationCombinesScopeCollegeAndClassFilters() throws Exception
    {
        bind(1);
        jdbc.update(
                "INSERT INTO student(uuid,student_no,name,class_id) VALUES(201,'S201','甲',1),(202,'S202','乙',1),(203,'S203','丙',2),(204,'S204','丁',3)");
        mvc.perform(get("/student/page").param("pageSize", "1").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.data.totalCount").value(3))
                .andExpect(jsonPath("$.data.total").value(3))
                .andExpect(jsonPath("$.data.records[0].uuid").value(204));
        mvc.perform(get("/student/page")
                            .param("collegeId", "1")
                            .param("pageNum", "2")
                            .param("pageSize", "1")
                            .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.data.totalCount").value(2))
                .andExpect(jsonPath("$.data.records[0].uuid").value(201));
        mvc.perform(get("/student/page").param("classId", "3").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.data.totalCount").value(1))
                .andExpect(jsonPath("$.data.records[0].uuid").value(204));
        mvc.perform(get("/student/page").param("classId", "2").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.data.totalCount").value(0));
        mvc.perform(get("/student/203").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.code").value(403));
        mvc.perform(get("/student/page").param("pageNum", "0").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.code").value(600));
        jdbc.update("DELETE FROM sys_user_role");
        bind(2);
        mvc.perform(get("/student/page").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.data.totalCount").value(3));
        bind(3);
        mvc.perform(get("/student/page").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.code").value(403));
        mvc.perform(get("/student/page")).andExpect(status().isUnauthorized());
    }

    @Test
    void studentsByUserFollowEnabledRoleAndRejectOtherUsers() throws Exception
    {
        bind(1);
        jdbc.update(
                "INSERT INTO student(uuid,student_no,name,class_id) VALUES(301,'S301','甲',1),(302,'S302','乙',2),(303,'S303','丙',3)");
        mvc.perform(get("/student/by-user/10").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].uuid").value(303))
                .andExpect(jsonPath("$.data[1].uuid").value(301));
        mvc.perform(get("/student/by-user/11").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.code").value(403));
        jdbc.update("DELETE FROM sys_user_role");
        bind(2);
        mvc.perform(get("/student/by-user/10").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].uuid").value(302))
                .andExpect(jsonPath("$.data[1].uuid").value(301));
        jdbc.update("DELETE FROM student");
        mvc.perform(get("/student/by-user/10").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isEmpty());
        jdbc.update("UPDATE sys_role SET status=0 WHERE id=2");
        mvc.perform(get("/student/by-user/10").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.code").value(403));
        bind(3);
        mvc.perform(get("/student/by-user/10").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.code").value(403));
        mvc.perform(get("/student/by-user/10")).andExpect(status().isUnauthorized());
    }

    @Test
    void filingQueuesFilterStageAndScopeAndArchivesStaySeparate() throws Exception
    {
        jdbc.execute("ALTER TABLE employment_filing ADD COLUMN IF NOT EXISTS student_uuid BIGINT");
        jdbc.execute("ALTER TABLE employment_filing ADD COLUMN IF NOT EXISTS status INT");
        jdbc.execute("ALTER TABLE employment_filing ADD COLUMN IF NOT EXISTS submitted_at TIMESTAMP");
        jdbc.execute(
                "ALTER TABLE employment_filing ADD COLUMN IF NOT EXISTS destination_type VARCHAR(30) DEFAULT 'EMPLOYMENT'");
        jdbc.update(
                "INSERT INTO student(uuid,student_no,name,class_id) VALUES(501,'S501','甲',1),(502,'S502','乙',2),(503,'S503','丙',3)");
        for (int cls = 1; cls <= 3; cls++)
        {
            for (int stage = 1; stage <= 4; stage++)
            {
                jdbc.update(
                        "INSERT INTO employment_filing(id,class_id,student_uuid,status,submitted_at) VALUES(?,?,?,?,CURRENT_TIMESTAMP)",
                        cls * 100 + stage, cls, 500 + cls, stage * 10);
            }
        }
        // 重复管理记录不能重复返回备案。
        jdbc.update("INSERT INTO counselor_class(counselor_id,class_id,role_type) VALUES(1,1,2)");
        bind(1);
        mvc.perform(get("/employment-filing/pending-review/10").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[*].id", Matchers.containsInAnyOrder(101, 301)))
                .andExpect(jsonPath("$.data[0].status").value(10))
                .andExpect(jsonPath("$.data[0].destinationType").value("EMPLOYMENT"));
        mvc.perform(get("/employment-filing/archived/10").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[*].id", Matchers.containsInAnyOrder(103, 303)));
        jdbc.update("DELETE FROM sys_user_role");
        bind(2);
        mvc.perform(get("/employment-filing/pending-review/10").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[*].id", Matchers.containsInAnyOrder(102, 202)))
                .andExpect(jsonPath("$.data[0].status").value(20));
        mvc.perform(get("/employment-filing/archived/10").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.data[*].id", Matchers.containsInAnyOrder(103, 203)));
        mvc.perform(get("/employment-filing/pending-review/11").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.code").value(403));
        mvc.perform(get("/employment-filing/archived/11").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.code").value(403));
        jdbc.update("DELETE FROM employment_filing");
        mvc.perform(get("/employment-filing/archived/10").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isEmpty());
        jdbc.update("UPDATE sys_role SET status=0 WHERE id=2");
        mvc.perform(get("/employment-filing/pending-review/10").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.code").value(403));
        bind(3);
        mvc.perform(get("/employment-filing/archived/10").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.code").value(403));
        mvc.perform(get("/employment-filing/pending-review/10")).andExpect(status().isUnauthorized());
        jdbc.update("UPDATE sys_user SET account_type='STUDENT',student_uuid=501,counselor_id=NULL WHERE id=10");
        String studentToken = TokenUserDTO.generateAccountToken("teacher", 10L, "STUDENT", 501L);
        mvc.perform(get("/employment-filing/pending-review/10").header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden());
        mvc.perform(get("/employment-filing/archived/10").header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden());
        jdbc.update("UPDATE sys_user SET account_type='STAFF',student_uuid=NULL,counselor_id=1 WHERE id=10");
        // 新增管理查询不放开原有学生提交和查询接口。
        mvc.perform(get("/employment-filing/getFilings").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void reviewChecksSnapshotScopeAndWritesStatusAndAuditAtomically() throws Exception
    {
        jdbc.execute("ALTER TABLE employment_filing ADD COLUMN IF NOT EXISTS student_uuid BIGINT");
        jdbc.execute("ALTER TABLE employment_filing ADD COLUMN IF NOT EXISTS status INT");
        jdbc.execute("ALTER TABLE employment_filing ADD COLUMN IF NOT EXISTS submitted_at TIMESTAMP");
        jdbc.execute("ALTER TABLE employment_filing ADD COLUMN IF NOT EXISTS archived_at TIMESTAMP");
        jdbc.execute("ALTER TABLE employment_filing ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP");
        jdbc.execute("ALTER TABLE employment_filing ADD COLUMN IF NOT EXISTS version INT DEFAULT 0");
        jdbc.update("UPDATE sys_user SET real_name='审核老师' WHERE id=10");
        jdbc.update(
                "INSERT INTO student(uuid,student_no,name,class_id) VALUES(701,'S701','学生',1),(702,'S702','其他班',2)");
        jdbc.update(
                "INSERT INTO employment_filing(id,class_id,student_uuid,status,version) VALUES(701,1,701,10,0),(702,2,702,10,0),(703,1,701,10,0),(704,1,701,10,0)");
        bind(1);
        String pass = "{\"filingId\":701,\"version\":0,\"fromStatus\":10,\"auditResult\":1}";
        mvc.perform(put("/employment-filing/review/counselor")
                            .header("Authorization", "Bearer " + token)
                            .contentType("application/json")
                            .content(pass.replace("\"version\":0", "\"version\":9")))
                .andExpect(jsonPath("$.code").value(409));
        mvc.perform(put("/employment-filing/review/counselor")
                            .header("Authorization", "Bearer " + token)
                            .contentType("application/json")
                            .content(pass.replace("701", "702")))
                .andExpect(jsonPath("$.code").value(403));
        mvc.perform(put("/employment-filing/review/department")
                            .header("Authorization", "Bearer " + token)
                            .contentType("application/json")
                            .content(pass))
                .andExpect(jsonPath("$.code").value(403));
        mvc.perform(put("/employment-filing/review/counselor")
                            .header("Authorization", "Bearer " + token)
                            .contentType("application/json")
                            .content(pass))
                .andExpect(jsonPath("$.code").value(200));
        assertEquals(20, jdbc.queryForObject("SELECT status FROM employment_filing WHERE id=701", Integer.class));
        assertEquals(1, jdbc.queryForObject("SELECT version FROM employment_filing WHERE id=701", Integer.class));
        assertEquals("审核老师",
                     jdbc.queryForObject("SELECT auditor_name FROM employment_audit_record WHERE filing_id=701",
                                         String.class));
        mvc.perform(put("/employment-filing/review/counselor")
                            .header("Authorization", "Bearer " + token)
                            .contentType("application/json")
                            .content(pass))
                .andExpect(jsonPath("$.code").value(409));
        String reject = "{\"filingId\":703,\"version\":0,\"fromStatus\":10,\"auditResult\":2}";
        mvc.perform(put("/employment-filing/review/counselor")
                            .header("Authorization", "Bearer " + token)
                            .contentType("application/json")
                            .content(reject))
                .andExpect(jsonPath("$.code").value(600));
        mvc.perform(put("/employment-filing/review/counselor")
                            .header("Authorization", "Bearer " + token)
                            .contentType("application/json")
                            .content(reject.replace("}", ",\"auditOpinion\":\"缺少材料\"}")))
                .andExpect(jsonPath("$.code").value(200));
        assertEquals(40, jdbc.queryForObject("SELECT status FROM employment_filing WHERE id=703", Integer.class));
        // 人为制造历史冲突，确认状态更新随记录写入失败一起回滚。
        jdbc.update("INSERT INTO employment_audit_record(filing_id,audit_stage) VALUES(704,1)");
        mvc.perform(put("/employment-filing/review/counselor")
                            .header("Authorization", "Bearer " + token)
                            .contentType("application/json")
                            .content(pass.replace("701", "704")))
                .andExpect(jsonPath("$.code").value(600));
        assertEquals(10, jdbc.queryForObject("SELECT status FROM employment_filing WHERE id=704", Integer.class));
        assertEquals(0, jdbc.queryForObject("SELECT version FROM employment_filing WHERE id=704", Integer.class));
        jdbc.update("DELETE FROM sys_user_role");
        bind(2);
        mvc.perform(put("/employment-filing/review/department")
                            .header("Authorization", "Bearer " + token)
                            .contentType("application/json")
                            .content("{\"filingId\":701,\"version\":1,\"fromStatus\":20,\"auditResult\":1}"))
                .andExpect(jsonPath("$.code").value(200));
        assertEquals(30, jdbc.queryForObject("SELECT status FROM employment_filing WHERE id=701", Integer.class));
        assertNotNull(jdbc.queryForObject("SELECT archived_at FROM employment_filing WHERE id=701", Timestamp.class));
        assertEquals(2, jdbc.queryForObject("SELECT COUNT(*) FROM employment_audit_record WHERE filing_id=701",
                                            Integer.class));
    }

    @Test
    void studentsAreFilteredByClassAndManagementScope() throws Exception
    {
        bind(1);
        jdbc.update(
                "INSERT INTO student(uuid,student_no,name,class_id) VALUES(101,'S101','学生甲',1),(102,'S102','学生乙',1),(103,'S103','其他班学生',2)");
        mvc.perform(get("/class/1/students").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].studentNo").value("S101"))
                .andExpect(jsonPath("$.data[1].classId").value(1))
                .andExpect(jsonPath("$.data[0].password").doesNotExist());
        mvc.perform(get("/class/2/students").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.code").value(403));
        mvc.perform(get("/class/3/students").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isEmpty());
        jdbc.update("DELETE FROM sys_user_role");
        bind(2);
        mvc.perform(get("/class/2/students").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.data[0].studentNo").value("S103"));
        mvc.perform(get("/class/3/students").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.code").value(403));
        bind(3);
        mvc.perform(get("/class/1/students").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void counselorSeesOnlyOwnPrimaryAndAssistingClasses() throws Exception
    {
        bind(1);
        mvc.perform(get("/class/managed").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].id").value(3))
                .andExpect(jsonPath("$.data[1].id").value(1));
        mvc.perform(get("/class/by-counselor/2").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.code").value(403));
    }
    @Test
    void departmentHeadSeesOnlyCollegeClasses() throws Exception
    {
        bind(2);
        mvc.perform(get("/class/managed").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].id").value(2))
                .andExpect(jsonPath("$.data[1].id").value(1));
        mvc.perform(get("/class/by-counselor/999").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].id").value(2));
    }
    @Test
    void studentRoleOverridesStaffRoles() throws Exception
    {
        bind(1);
        bind(3);
        mvc.perform(get("/class/managed").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.data").isEmpty());
    }
    @Test
    void studentAccountCannotUseEitherRouteEvenWithStaffRole() throws Exception
    {
        jdbc.update("INSERT INTO student(uuid,student_no,name) VALUES(100,'S100','学生')");
        jdbc.update("UPDATE sys_user SET account_type='STUDENT',student_uuid=100,counselor_id=NULL WHERE id=10");
        bind(2);
        String studentToken = TokenUserDTO.generateAccountToken("teacher", 10L, "STUDENT", 100L);
        mvc.perform(get("/class/managed").header("Authorization", "Bearer " + studentToken))
                .andExpect(jsonPath("$.code").value(403));
        mvc.perform(get("/class/by-counselor/1").header("Authorization", "Bearer " + studentToken))
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void absentOrDisabledRolesAreDenied() throws Exception
    {
        mvc.perform(get("/class/managed").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.code").value(403));
        bind(1);
        jdbc.update("UPDATE sys_role SET status=0 WHERE id=1");
        mvc.perform(get("/class/managed").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.code").value(403));
        mvc.perform(get("/class/managed")).andExpect(status().isUnauthorized());
    }
    @Test
    void noAssignedClassesReturnsEmptyList() throws Exception
    {
        bind(1);
        jdbc.update("DELETE FROM counselor_class");
        mvc.perform(get("/class/managed").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isEmpty());
    }
}
