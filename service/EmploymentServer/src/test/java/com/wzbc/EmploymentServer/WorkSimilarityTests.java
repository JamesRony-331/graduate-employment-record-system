package com.wzbc.EmploymentServer;

import com.wzbc.EmploymentServer.Service.impl.WorkSelectServiceImpl;
import com.wzbc.EmploymentServer.entity.DTO.WorkSelect.WorkSelectDTO;
import com.wzbc.EmploymentServer.entity.PO.WorkComparePO;
import com.wzbc.EmploymentServer.Mapper.WorkCompareMapper;
import org.springframework.test.util.ReflectionTestUtils;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WorkSimilarityTests
{
    private final WorkSelectServiceImpl service = new WorkSelectServiceImpl();
    @Test
    void educationAndExperienceAreDirectional()
    {
        assertEquals(1.0, service.judgeEducation("本科", "大专"));
        assertEquals(1.0, service.judgeEducation("硕士", "本科"));
        assertEquals(0.0, service.judgeEducation("大专", "本科"));
        assertEquals(0.0, service.judgeEducation("本科", null));
        assertEquals(1.0, service.judgeExperience(3, 1));
        assertEquals(0.0, service.judgeExperience(0, 1));
    }
    @Test
    void skillsMeasureRequirementCoverage()
    {
        assertEquals(1.0, service.judgeSkills(List.of("Java", "SQL", "Python"), List.of("Java", "SQL")));
        assertEquals(0.5, service.judgeSkills(List.of("Java"), List.of("Java", "SQL")));
        assertNull(service.judgeCities(List.of("不限"), List.of("杭州")));
        assertNull(service.judgeWorkMode(null, "远程"));
        assertEquals(1.0, service.judgeRestDays("单休", "双休"));
    }
    @Test
    void salaryAndHardPreferencesFilter()
    {
        var u = new WorkSelectDTO();
        var j = new WorkComparePO();
        u.setMinimumSalary(new BigDecimal("6000"));
        j.setMinimumSalary(new BigDecimal("5000"));
        assertFalse(service.meetsRequiredConditions(u, j));
        j.setMinimumSalary(new BigDecimal("7000"));
        assertTrue(service.meetsRequiredConditions(u, j));
        var strict = new WorkSelectDTO.StrictConditions();
        strict.setCities(true);
        u.setStrict(strict);
        u.setCities(List.of("杭州"));
        j.setCities(List.of("上海"));
        assertFalse(service.meetsRequiredConditions(u, j));
        j.setCities(List.of("杭州"));
        assertTrue(service.meetsRequiredConditions(u, j));
        assertEquals(1.0, service.judgeExpectedSalary(new BigDecimal("6000"), new BigDecimal("8000")));
    }
    @Test
    void recommendationsPreserveCompanyAndUseStableOrder()
    {
        var mapper = mock(WorkCompareMapper.class);
        ReflectionTestUtils.setField(service, "workCompareMapper", mapper);
        var u = new WorkSelectDTO();
        u.setEducation("本科");
        var a = new WorkComparePO();
        a.setId(1L);
        a.setEducation("大专");
        a.setCompanyName("测试公司");
        a.setCompanyContact("jobs@example.com");
        var b = new WorkComparePO();
        b.setId(2L);
        b.setEducation("硕士");
        when(mapper.selectAll()).thenReturn(List.of(b, a));
        var result = service.getRecommendedJobs(u);
        assertEquals(1L, result.getFirst().getId());
        assertEquals(100, result.getFirst().getSimilarity());
        assertEquals("jobs@example.com", result.getFirst().getCompanyContact());
        u.setOther("不参与评分");
        assertEquals(100, service.calculateSimilarity(u, a));
    }
}
