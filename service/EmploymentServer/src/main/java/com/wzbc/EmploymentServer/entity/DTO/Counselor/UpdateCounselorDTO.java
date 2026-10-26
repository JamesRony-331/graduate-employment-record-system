package com.wzbc.EmploymentServer.entity.DTO.Counselor;

import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Data
public class UpdateCounselorDTO
{

    @NotNull(message = "辅导员ID不能为空")
    @Positive(message = "辅导员ID必须为正数")
    private Long id;
    /** 性别：0 未知，1 男，2 女。 */
    @Min(value = 0, message = "性别只能为0、1或2")
    @Max(value = 2, message = "性别只能为0、1或2")
    private Integer gender;
    /** 联系电话。 */
    @Size(max = 20, message = "联系电话不能超过20个字符")
    private String phone;
    /** 电子邮箱。 */
    @Email(message = "电子邮箱格式不正确")
    @Size(max = 100, message = "电子邮箱不能超过100个字符")
    private String email;
    /** 状态：1 在职，0 离职。 */
    @Min(value = 0, message = "状态只能为0或1")
    @Max(value = 1, message = "状态只能为0或1")
    private Integer status;
}
