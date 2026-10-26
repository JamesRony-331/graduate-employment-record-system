package com.wzbc.EmploymentServer;

import com.wzbc.EmploymentServer.Service.EmploymentAuditRecordService;
import com.wzbc.EmploymentServer.entity.PO.EmploymentAuditRecordPO;
import com.wzbc.EmploymentServer.Exception.BusinessException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        properties =
                {"spring.config.import=",
                 "spring.datasource.url=jdbc:h2:mem:audit-record-tests;MODE=MySQL;DB_CLOSE_DELAY=-1",
                 "spring.datasource.username=sa", "spring.datasource.password=",
                 "spring.datasource.driver-class-name=org.h2.Driver", "spring.sql.init.mode=always",
                 "spring.sql.init.schema-locations=classpath:account-schema.sql,classpath:counselor-class-schema.sql"
                 })
class EmploymentAuditRecordServiceTests
{
    @Autowired
    EmploymentAuditRecordService service;
    @Autowired
    JdbcTemplate jdbc;
    @Autowired
    PlatformTransactionManager transactions;
    @BeforeEach
    void setup()
    {
        jdbc.execute(
                "CREATE TABLE IF NOT EXISTS employment_audit_record(id BIGINT AUTO_INCREMENT PRIMARY KEY,filing_id BIGINT NOT NULL REFERENCES employment_filing(id),audit_stage INT NOT NULL,auditor_id BIGINT NOT NULL,auditor_name VARCHAR(64) NOT NULL,auditor_role INT NOT NULL,audit_result INT NOT NULL,audit_opinion VARCHAR(1000),from_status INT NOT NULL,to_status INT NOT NULL,audited_at TIMESTAMP NOT NULL,UNIQUE(filing_id,audit_stage))");
        jdbc.update("DELETE FROM employment_audit_record");
        jdbc.update("DELETE FROM employment_filing");
        jdbc.update("INSERT INTO employment_filing(id,class_id) VALUES(1,1)");
    }
    EmploymentAuditRecordPO initial()
    {
        var record = new EmploymentAuditRecordPO();
        record.setFilingId(1L)
        ;
        record.setAuditStage(1)
        ;
        record.setAuditorId(10L)
        ;
        record.setAuditorName("审核老师")
        ;
        record.setAuditorRole(1)
        ;
        record.setAuditResult(1)
        ;
        record.setFromStatus(10)
        ;
        record.setToStatus(20)
        ;
        return record;
    }
    @Test
    void supportsCrudHistoryAndPagination()
    {
        var id = service.add(initial());
        var record = service.selectById(id);
        assertEquals("审核老师", record.getAuditorName());
        assertNotNull(record.getAuditedAt());
        var time = record.getAuditedAt();
        record.setAuditOpinion("材料齐全")
        ;
        service.update(record);
        assertEquals("材料齐全", service.selectById(id).getAuditOpinion());
        assertEquals(time, service.selectById(id).getAuditedAt());
        var review = initial();
        review.setAuditStage(2);
        review.setAuditorRole(2);
        review.setFromStatus(20);
        review.setToStatus(30);
        service.add(review);
        assertEquals(2, service.selectByFilingId(1L).size());
        assertEquals(2, service.selectByPage(1, 1).totalCount());
        assertEquals(id, service.selectByPage(2, 1).records().getFirst().getId());
        service.delete(id);
        assertThrows(BusinessException.class, () -> service.selectById(id));
        assertThrows(BusinessException.class, () -> service.delete(id));
    }
    @Test
    void validatesTransitionsAndJoinsCallerTransaction()
    {
        var rejected = initial();
        rejected.setAuditResult(2);
        rejected.setToStatus(40);
        assertThrows(BusinessException.class, () -> service.add(rejected));
        rejected.setAuditOpinion("缺少材料");
        Long id = service.add(rejected);
        assertThrows(BusinessException.class, () -> service.add(initial()));
        var invalid = initial();
        invalid.setFilingId(999L);
        assertThrows(BusinessException.class, () -> service.add(invalid));
        invalid.setFilingId(1L);
        invalid.setToStatus(30);
        assertThrows(BusinessException.class, () -> service.add(invalid));
        assertThrows(BusinessException.class, () -> service.selectByPage(0, 10));
        service.delete(id);
        var transaction = new TransactionTemplate(transactions);
        assertThrows(IllegalStateException.class, () -> transaction.execute(status ->
        {
            service.add(initial());
            throw new IllegalStateException("模拟备案状态更新失败");
        }));
        assertTrue(service.selectByFilingId(1L).isEmpty());
    }
}
