package com.wzbc.EmploymentServer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wzbc.EmploymentServer.Exception.BusinessException;
import com.wzbc.EmploymentServer.Mapper.WorkCompareMapper;
import com.wzbc.EmploymentServer.Mapper.BasicKnowledgeMapper;
import com.wzbc.EmploymentServer.entity.PO.BasicKnowledgePO;
import com.wzbc.EmploymentServer.Service.impl.AiServiceImpl;
import com.wzbc.EmploymentServer.util.AiUtils;
import com.wzbc.EmploymentServer.entity.PO.WorkComparePO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AiServiceTests
{
    private AiServiceImpl service;
    private WorkCompareMapper mapper;
    private AiUtils aiUtils;
    private BasicKnowledgeMapper knowledgeMapper;

    @BeforeEach
    void setup()
    {
        service = new AiServiceImpl();
        aiUtils = mock(AiUtils.class);
        ReflectionTestUtils.setField(service, "aiUtils", aiUtils);
        mapper = mock(WorkCompareMapper.class);
        knowledgeMapper = mock(BasicKnowledgeMapper.class);
        when(knowledgeMapper.selectAllEnabled()).thenReturn(List.of());
        ReflectionTestUtils.setField(service, "basicKnowledgeMapper", knowledgeMapper);
        ReflectionTestUtils.setField(service, "objectMapper", new ObjectMapper().findAndRegisterModules());
        ReflectionTestUtils.setField(service, "workCompareMapper", mapper);
    }

    @Test
    void knowledgeAnswerReturnsAfterFirstAiCall()
    {
        var knowledge = new BasicKnowledgePO();
        knowledge.setId(1L);
        knowledge.setTitle("就业备案流程");
        knowledge.setAnswer("提交备案后等待辅导员审核");
        knowledge.setStatus(1);
        when(knowledgeMapper.selectAllEnabled()).thenReturn(List.of(knowledge));
        doReturn("{\"needDatabase\":false,\"answer\":\"提交备案后等待辅导员审核\"}").when(aiUtils).askAI(anyString());

        assertEquals(knowledge.getAnswer(), service.getAnswer("就业备案流程是什么"));
        verify(knowledgeMapper).selectAllEnabled();
        verifyNoInteractions(mapper);
        var prompt = ArgumentCaptor.forClass(String.class);
        verify(aiUtils, times(1)).askAI(prompt.capture());
        assertTrue(prompt.getValue().contains("\"title\":\"就业备案流程\""));
        assertTrue(prompt.getValue().contains("\"answer\":\"提交备案后等待辅导员审核\""));
    }

    @Test
    void generalAdviceDoesNotQueryDatabase()
    {
        doReturn("{\"needDatabase\":false,\"answer\":\"请准备自我介绍\"}").when(aiUtils).askAI(anyString());
        assertEquals("请准备自我介绍", service.getAnswer("如何准备面试"));
        verifyNoInteractions(mapper);
        verify(aiUtils, times(1)).askAI(anyString());
    }

    @Test
    void jobQuestionIncludesDatabaseDataInSecondRequest()
    {
        var job = new WorkComparePO();
        job.setId(7L);
        job.setCities(List.of("杭州"));
        when(mapper.selectAll()).thenReturn(List.of(job));
        doReturn("{\"needDatabase\":true,\"answer\":\"\"}", "推荐杭州岗位").when(aiUtils).askAI(anyString());
        assertEquals("推荐杭州岗位", service.getAnswer("查找杭州岗位"));
        verify(mapper).selectAll();
        var prompts = ArgumentCaptor.forClass(String.class);
        verify(aiUtils, times(2)).askAI(prompts.capture());
        String second = prompts.getAllValues().get(1);
        assertTrue(second.contains("查找杭州岗位"));
        assertTrue(second.contains("\"id\":7"));
        assertTrue(second.contains("\"cities\":[\"杭州\"]"));
    }

    @Test
    void emptyDatabaseIsStillPassedToAi()
    {
        when(mapper.selectAll()).thenReturn(List.of());
        doReturn("{\"needDatabase\":true}", "暂无岗位").when(aiUtils).askAI(anyString());
        assertEquals("暂无岗位", service.getAnswer("推荐岗位"));
        verify(aiUtils).askAI(contains("岗位数据库查询结果（JSON 数组）：\n[]"));
    }

    @Test
    void invalidDecisionDoesNotQueryDatabase()
    {
        for (String response : List.of("不是JSON", "null", "{}", "{\"needDatabase\":\"true\"}",
                                       "{\"needDatabase\":false,\"answer\":\"\"}"))
        {
            doReturn(response).when(aiUtils).askAI(anyString());
            assertThrows(BusinessException.class, () -> service.getAnswer("推荐岗位"));
        }
        verifyNoInteractions(mapper);
    }
}
