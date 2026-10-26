package com.wzbc.EmploymentServer.entity.DTO.User;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.Data;

@Data
public class UserRegDTO
{
    @NotBlank
    @Size(max = 32)
    private String studentNo;
    @NotBlank
    @Size(max = 50)
    private String name;
    private Integer gender;
    @NotBlank
    @Pattern(regexp = "[0-9]{17}[0-9Xx]")
    private String idCard;
    private Long collegeId;
    private Long majorId;
    private Integer enrollmentYear;
    private Integer graduationYear;
    private String educationLevel;
    private Integer studentStatus;
    private Long classId;
}
