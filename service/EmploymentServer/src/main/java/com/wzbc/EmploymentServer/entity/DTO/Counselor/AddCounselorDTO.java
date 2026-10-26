package com.wzbc.EmploymentServer.entity.DTO.Counselor;

import lombok.Data;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

@Data
public class AddCounselorDTO
{
    /** 辅导员工号。 */
    @NotBlank(message = "辅导员工号不能为空")
    @Size(max = 30)
    private String counselorNo;
    /** 姓名。 */
    @NotBlank(message = "姓名不能为空")
    @Size(max = 50)
    private String name;
    /** 登录账号，不传时使用辅导员工号。 */
    @Size(max = 50)
    private String username;
    /** 不传或为null时使用默认密码123456。 */
    @Size(min = 6, max = 72, message = "密码长度应为6到72个字符")
    private String password;
    /** 性别：0 未知，1 男，2 女。 */
    private Integer gender;
    /** 联系电话。 */
    private String phone;
    /** 电子邮箱。 */
    private String email;
    /** 入职日期。 */
    private LocalDate hireDate;
}
