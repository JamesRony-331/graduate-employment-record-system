package com.wzbc.EmploymentServer.entity.PO;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 就业审核记录持久化对象。
 */
@Data
public class EmploymentAuditRecordPO
{
    /** 审核记录主键 */
    private Long id;

    /** 对应本次提交的备案ID */
    private Long filingId;

    /** 1初审阶段 2复审阶段 */
    private Integer auditStage;

    /** 审核人ID，由业务关联管理端账号 */
    private Long auditorId;

    /** 审核人姓名快照 */
    private String auditorName;

    /** 1辅导员 2院系审核人员 */
    private Integer auditorRole;

    /** 1通过 2打回 */
    private Integer auditResult;

    /** 审核意见，打回时必填 */
    private String auditOpinion;

    /** 审核前备案状态 */
    private Integer fromStatus;

    /** 审核后备案状态 */
    private Integer toStatus;

    /** 审核时间 */
    private LocalDateTime auditedAt;
}
