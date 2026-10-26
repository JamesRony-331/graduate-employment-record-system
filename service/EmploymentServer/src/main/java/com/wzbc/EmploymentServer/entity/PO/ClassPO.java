package com.wzbc.EmploymentServer.entity.PO;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ClassPO
{
    private Long id;
    private String classCode;
    private String className;
    private Long collegeId;
    private Long majorId;
    /** 由专业表查询取得，仅用于展示。 */
    private String majorName;
    private Integer enrollmentYear;
    private Integer studentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
