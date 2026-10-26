package com.wzbc.EmploymentServer.entity.DTO.WorkSelect;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/** 岗位推荐条件。可选字段不设置默认偏好，缺省值保持 null。 */
@Data
public class WorkSelectDTO
{
    @NotBlank(message = "请选择最高学历")
    @Pattern(regexp = "高中及以下|中专 / 职高|大专|本科|硕士|博士")
    private String education;

    @Size(max = 100)
    private String major;

    /** 工作年限，应届生填写 0。 */
    @NotNull
    @Min(0)
    @Max(60)
    private Integer experience;

    @NotEmpty
    @Size(max = 100)
    private List<@NotBlank @Size(max = 100) String> skills;

    @NotEmpty
    @Size(max = 100)
    private List<@NotBlank @Size(max = 100) String> categories;

    /** “不限”不可与具体城市同时提交。 */
    @NotEmpty
    @Size(max = 100)
    private List<@NotBlank @Size(max = 100) String> cities;

    @NotBlank
    @Pattern(regexp = "全职|实习|兼职")
    private String jobType;

    /** 元/月；填写后作为底线。 */
    @DecimalMin("0")
    @DecimalMax("10000000")
    @Digits(integer = 8, fraction = 2)
    private BigDecimal minimumSalary;

    /** 元/月；用于排序。 */
    @DecimalMin("0")
    @DecimalMax("10000000")
    @Digits(integer = 8, fraction = 2)
    private BigDecimal expectedSalary;

    @Pattern(regexp = "|现场办公|远程|混合办公")
    private String workMode;
    @Pattern(regexp = "|双休|单双休|单休")
    private String restDays;
    @Pattern(regexp = "|不接受|接受偶尔出差|不限")
    private String travel;
    @Pattern(regexp = "|不接受|接受|不限")
    private String outsourcing;

    @NotBlank
    @Pattern(regexp = "薪资|技能匹配|地点|休息制度|专业相关性|工作方式|发展空间")
    private String priority;

    /** 第一版仅展示，不自动解析为筛选条件。 */
    @Size(max = 1000)
    private String other;

    @Valid
    private StrictConditions strict;

    @Data
    public static class StrictConditions
    {
        private Boolean cities;
        private Boolean workMode;
        private Boolean restDays;
        private Boolean travel;
        private Boolean outsourcing;
    }

    @JsonIgnore
    @AssertTrue(message = "期望月薪不能低于最低可接受月薪")
    public boolean isSalaryRangeValid()
    {
        return minimumSalary == null || expectedSalary == null || expectedSalary.compareTo(minimumSalary) >= 0;
    }

    @JsonIgnore
    @AssertTrue(message = "不限不可与具体城市同时选择")
    public boolean isCitiesSelectionValid()
    {
        return cities == null || !cities.contains("不限") || cities.size() == 1;
    }

    @JsonIgnore
    @AssertTrue(message = "必须满足开关需要对应的具体条件，空值或不限不能作为硬性条件")
    public boolean isStrictConditionsValid()
    {
        if (strict == null)
            return true;
        return (!Boolean.TRUE.equals(strict.cities) ||
                (cities != null && !cities.isEmpty() && cities.stream().allMatch(WorkSelectDTO::specific))) &&
                (!Boolean.TRUE.equals(strict.workMode) || specific(workMode)) &&
                (!Boolean.TRUE.equals(strict.restDays) || specific(restDays)) &&
                (!Boolean.TRUE.equals(strict.travel) || specific(travel)) &&
                (!Boolean.TRUE.equals(strict.outsourcing) || specific(outsourcing));
    }

    private static boolean specific(String value)
    {
        return value != null && !value.isBlank() && !"不限".equals(value);
    }
}
