package com.wzbc.EmploymentServer.entity.DTO.Student;

import com.wzbc.EmploymentServer.entity.DTO.User.UpdateUserMsgAdmin;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateStudentDTO extends UpdateUserMsgAdmin
{
    @Size(min = 1, max = 50)
    @Pattern(regexp = ".*\\S.*", message = "姓名不能为空白")
    private String name;
    @Override
    @NotNull
    @Positive
    public Long getUuid()
    {
        return super.getUuid();
    }
}
