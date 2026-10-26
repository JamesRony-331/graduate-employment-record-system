package com.wzbc.EmploymentServer.entity.PO;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** 对比记录，业务字段与 WorkSelectDTO 一致。 */
@Data
public class WorkComparePO
{
    private Long id;
    private String companyName;
    private String companyContact;
    private String education;
    private String major;
    private Integer experience;
    private List<String> skills;
    private List<String> categories;
    private List<String> cities;
    private String jobType;
    private BigDecimal minimumSalary;
    private BigDecimal expectedSalary;
    private String workMode;
    private String restDays;
    private String travel;
    private String outsourcing;
    private String priority;
    private String other;
    private StrictConditions strict;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @Data
    public static class StrictConditions
    {
        private Boolean cities;
        private Boolean workMode;
        private Boolean restDays;
        private Boolean travel;
        private Boolean outsourcing;
    }
}
