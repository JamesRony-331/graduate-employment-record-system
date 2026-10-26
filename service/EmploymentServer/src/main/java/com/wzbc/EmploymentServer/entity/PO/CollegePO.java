package com.wzbc.EmploymentServer.entity.PO;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CollegePO
{
    private Long id;
    private String collegeCode;
    private String collegeName;
    private String deanName;
    private String phone;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
