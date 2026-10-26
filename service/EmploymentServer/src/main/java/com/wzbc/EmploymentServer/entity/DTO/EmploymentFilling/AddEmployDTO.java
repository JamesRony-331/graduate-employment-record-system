package com.wzbc.EmploymentServer.entity.DTO.EmploymentFilling;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class AddEmployDTO
{
    private String changeReason;

    /** 单位名称 */
    private String companyName;

    /** 单位统一社会信用代码 */
    private String companyCreditCode;

    /** 单位行业字典编码 */
    private String companyIndustry;

    /** 单位性质字典编码 */
    private String companyNature;

    /** 就业岗位 */
    private String jobTitle;

    /** 工作省份 */
    private String workProvince;

    /** 工作城市 */
    private String workCity;

    /** 工作详细地址 */
    private String workAddress;

    /** 月薪元，未填为NULL */
    private BigDecimal monthlySalary;

    /** 签约日期 */
    private LocalDate signedDate;

    /** 联系电话快照 */
    private String contactPhone;

    /** 联系邮箱快照 */
    private String contactEmail;

    /** 其他去向详情对象，如升学院校专业，以 JSON 字符串持久化 */
    private String destinationDetails;

    /** 学生备注 */
    private String remark;
}
