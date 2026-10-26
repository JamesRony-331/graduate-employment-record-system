package com.wzbc.EmploymentServer.entity.VO.Counselor;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CounselorVO
{
    /** 辅导员 ID。 */
    private Long id;
    /** 辅导员工号。 */
    private String counselorNo;
    /** 姓名。 */
    private String name;
    /** 性别：0 未知，1 男，2 女。 */
    private Integer gender;
    /** 联系电话。 */
    private String phone;
    /** 电子邮箱。 */
    private String email;
    /** 入职日期。 */
    private LocalDate hireDate;
    /** 状态：1 在职，0 离职。 */
    private Integer status;
    //private List<ClassVO> classes;
}
