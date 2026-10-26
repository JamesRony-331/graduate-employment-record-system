package com.wzbc.EmploymentServer.entity.DTO.WorkSelect;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class WorkCompareDTO extends WorkSelectDTO
{
    @Size(max = 200)
    private String companyName;
    @Size(max = 200)
    @Pattern(regexp = "^$|^[+0-9() -]{5,30}$|^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
             message = "联系方式请填写电话或邮箱")
    private String companyContact;
}
