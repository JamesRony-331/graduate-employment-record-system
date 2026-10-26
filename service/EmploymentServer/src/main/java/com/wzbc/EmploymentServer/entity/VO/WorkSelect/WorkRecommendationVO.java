package com.wzbc.EmploymentServer.entity.VO.WorkSelect;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** 岗位推荐结果，包含岗位信息及匹配相似度。 */
@Data
public class WorkRecommendationVO
{
    private Long id;
    private String companyName;
    private String companyContact;
    /** 相似度，范围 0 至 100，保留两位小数。 */
    private double similarity;
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
