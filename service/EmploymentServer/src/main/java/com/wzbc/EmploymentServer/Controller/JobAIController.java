package com.wzbc.EmploymentServer.Controller;

import com.wzbc.EmploymentServer.Service.AiService;
import com.wzbc.EmploymentServer.entity.DTO.JobAI.JobAIPromptDTO;
import com.wzbc.EmploymentServer.entity.VO.ResponseVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/job-ai")
public class JobAIController extends ABaseController
{

    @Autowired
    private AiService aiService;
    /** 接收提示词，由 AI 按需查询岗位资料并回答。 */
    @PostMapping("/prompt")
    public ResponseVO<String> receivePrompt(@Valid @RequestBody JobAIPromptDTO dto)
    {
        String answer = aiService.getAnswer(dto.getPrompt());
        return getSuccessResponseVO(answer);
    }
}
