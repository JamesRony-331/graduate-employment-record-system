package com.wzbc.EmploymentServer.entity.PO;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 就业备案持久化对象，一次提交对应一条记录。
 */
@Data
public class EmploymentFilingPO
{
    /** 备案主键 */
    private Long id;

    /** 服务端生成的申请编号 */
    private String applicationNo;

    /** 关联student.uuid，由业务校验 */
    private Long studentUuid;

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

    /** 变更或重提所关联的前一条备案 */
    private Long previousFilingId;

    /** 变更原因 */
    private String changeReason;

    /** 毕业去向字典编码，具体分类待定 */
    private String destinationType;

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

    /** 材料引用数组，保存fileId、name、type等，以 JSON 字符串持久化 */
    private String materials;

    /** 学生备注 */
    private String remark;

    /** 10待辅导员初审 20待院系复审 30已归档 40已打回 */
    private Integer status;

    /** 提交时间，不随审核更新 */
    private LocalDateTime submittedAt;

    /** 复审通过后自动进入归档状态的时间 */
    private LocalDateTime archivedAt;

    /** 乐观锁版本 */
    private Integer version;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;

    /** 数据库生成列，仅供读取，INSERT/UPDATE 时必须排除此列 */
    private Long pendingStudentUuid;
}
