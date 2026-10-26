package com.wzbc.EmploymentServer.entity.VO.EmploymentFiling;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class EmploymentFillingVO
{

    /** 提交时学号快照 */
    private String studentNo;

    /** 提交时姓名快照 */
    private String studentName;

    /** 学院快照 */
    private String college;

    /** 专业快照 */
    private String major;

    /** 班级ID */
    private Long classId;

    /** 班级名称快照 */
    private String className;

    /** 毕业年份 */
    private Integer graduationYear;

    /** 1首次备案 2变更申请 3打回重提 */
    private Integer applicationType;

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

    /** 学生备注 */
    private String remark;

    /** 10待辅导员初审 20待院系复审 30已归档 40已打回 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
