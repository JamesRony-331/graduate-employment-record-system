package com.wzbc.EmploymentServer.entity.PO;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
public class UserPO
{
    private Long uuid;
    private String studentNo;
    private String name;
    private Integer gender;
    private Date birthDate;
    private String idCard;
    private String phone;
    private String email;
    private Long collegeId;
    /** 由学院表查询取得，仅用于展示。 */
    private String college;
    private Long majorId;
    /** 由专业表查询取得，仅用于展示。 */
    private String major;
    /** 查询时由 class_id 关联班级表取得，不是 student 表字段。 */
    private String className;
    private Integer enrollmentYear;
    private Integer graduationYear;
    private String educationLevel;
    private Integer studentStatus;
    private String address;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long classId;
}
