package com.wzbc.EmploymentServer.entity.PO;

import lombok.Data;
import java.time.LocalDateTime;

/** 对应 counselor_class。 */
@Data
public class CounselorClassPO
{
    private Long id;
    private Long counselorId;
    private Long classId;
    /** 1主辅导员，2协管辅导员。 */
    private Integer roleType;
    private LocalDateTime assignedAt;
}
