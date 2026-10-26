package com.wzbc.EmploymentServer.entity.VO.User;

import lombok.Data;

import java.util.Date;

@Data
public class UserMsgVO
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
    private String className;
    private Integer enrollmentYear;
    private Integer graduationYear;
    private String educationLevel;
    private Integer studentStatus;
    private String address;
    private Long classId; //先保留id 后期连表查询辅导员和班级名称
}
