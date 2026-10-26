package com.wzbc.EmploymentServer.entity.DTO.JobAI;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** AI 提示词请求。 */
@Data
public class JobAIPromptDTO
{
    @NotBlank(message = "提示词不能为空")
    private String prompt;
}
