package com.wzbc.EmploymentServer.entity.DTO.User;

import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class UpdateUserMsgAdmin
{
    private Long uuid;
    @Pattern(regexp = "[0-9]{17}[0-9Xx]", message = "身份证号须为18位")
    private String idCard;
    @Pattern(regexp = "[0-9+() -]{6,20}", message = "请输入有效联系电话")
    private String phone;
    @Email
    @Size(max = 128)
    @Pattern(regexp = ".*\\S.*", message = "邮箱不能为空白")
    private String email;
    @Positive
    private Long collegeId;
    @Positive
    private Long majorId;
    @Min(1900)
    @Max(9999)
    private Integer enrollmentYear;
    @Min(1900)
    @Max(9999)
    private Integer graduationYear;
    @Size(max = 32)
    private String educationLevel;
    private Integer studentStatus;
    @Size(max = 255)
    private String address;
    @Positive
    private Long classId;
}
