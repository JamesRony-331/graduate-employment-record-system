package com.wzbc.EmploymentServer.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.volcengine.ark.runtime.model.responses.content.InputContentItemText;
import com.volcengine.ark.runtime.model.responses.item.ItemEasyMessage;
import com.volcengine.ark.runtime.model.responses.item.MessageContent;
import com.volcengine.ark.runtime.model.responses.request.CreateResponsesRequest;
import com.volcengine.ark.runtime.model.responses.request.ResponsesInput;
import com.volcengine.ark.runtime.model.responses.response.ResponseObject;
import com.volcengine.ark.runtime.model.responses.constant.ResponsesConstants;
import com.volcengine.ark.runtime.service.ArkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** 封装 AI 请求、响应文本解析及客户端资源释放。 */
@Component
public class AiUtils
{
    @Autowired
    private ObjectMapper objectMapper;

    @Value("${ark.api-key}")
    private String apiKey;

    private static final String SYSTEM_PROMPT = """
            你是一名高校就业指导助手，服务对象是即将毕业的大学生。
            你的职责是提供求职、简历、面试和就业备案方面的帮助。

            回答要求：
            1. 使用中文回答，语气耐心，表达简洁。
            2. 根据用户的实际情况提供具体建议。
            3. 用户信息不足时，询问必要的信息，不要自行猜测。
            4. 不得编造岗位、企业信息或用户的就业备案记录。
            5. 涉及系统内的数据时，只能依据后端提供的数据回答。
            6. 没有提供相关数据时，明确说明无法确认，不要声称已经查询数据库。
            """;

    public ResponseObject generateText(String prompt)
    {
        ArkService arkService =
                ArkService.builder().apiKey(apiKey).baseUrl("https://ark.cn-beijing.volces.com/api/v3").build();

        try
        {
            CreateResponsesRequest request =
                    CreateResponsesRequest.builder()
                            .model("doubao-seed-evolving")
                            .input(ResponsesInput.builder()
                                           .addListItem(
                                                   ItemEasyMessage.builder()
                                                           .role("system")
                                                           .content(MessageContent.builder()
                                                                            .addListItem(InputContentItemText.builder()
                                                                                                 .text(SYSTEM_PROMPT)
                                                                                                 .build())
                                                                            .build())
                                                           .build())
                                           .addListItem(
                                                   ItemEasyMessage.builder()
                                                           .role(ResponsesConstants.MESSAGE_ROLE_USER)
                                                           .content(MessageContent.builder()
                                                                            .addListItem(InputContentItemText.builder()
                                                                                                 .text(prompt)
                                                                                                 .build())
                                                                            .build())
                                                           .build())
                                           .build())
                            .build();

            return arkService.createResponse(request);
        }
        finally
        {
            arkService.shutdownExecutor();
        }
    }

    public String askAI(String prompt)
    {
        ResponseObject response = generateText(prompt);
        JsonNode root = objectMapper.valueToTree(response);

        StringBuilder answer = new StringBuilder();
        for (JsonNode item : root.path("output"))
        {
            if ("message".equals(item.path("type").asText()))
            {
                for (JsonNode content : item.path("content"))
                {
                    if ("output_text".equals(content.path("type").asText()))
                    {
                        answer.append(content.path("text").asText(""));
                    }
                }
            }
        }

        return answer.toString();
    }
}
