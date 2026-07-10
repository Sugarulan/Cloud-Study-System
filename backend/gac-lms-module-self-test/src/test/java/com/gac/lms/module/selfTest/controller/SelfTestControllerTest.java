package com.gac.lms.module.selfTest.controller;

import com.gac.lms.common.exception.BusinessException;
import com.gac.lms.common.exception.GlobalExceptionHandler;
import com.gac.lms.common.response.ErrorCode;
import com.gac.lms.common.response.PageResult;
import com.gac.lms.module.selfTest.service.SelfTestService;
import com.gac.lms.module.selfTest.vo.ExamItemVO;
import com.gac.lms.module.selfTest.vo.WrongQuestionVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * SelfTestController 单元测试。
 *
 * @author 方雨菲
 */
class SelfTestControllerTest {

    private MockMvc mockMvc;
    private SelfTestService selfTestService;

    private static final Long USER_ID = 8888L;
    private static final String USER_HEADER = "X-User-Id";

    @BeforeEach
    void setUp() {
        selfTestService = mock(SelfTestService.class);
        SelfTestController controller = new SelfTestController(selfTestService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /health")
    void health_ok() throws Exception {
        mockMvc.perform(get("/api/v1/self-test/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value("self-test-module-ok"));
    }

    @Test
    @DisplayName("GET /exams/pending - 缺 X-User-Id：UNAUTHORIZED")
    void pendingExams_missingUser() throws Exception {
        mockMvc.perform(get("/api/v1/self-test/exams/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.UNAUTHORIZED.getCode()));
    }

    @Test
    @DisplayName("GET /exams/pending - 正常返回空（W2 占位）")
    void pendingExams_success() throws Exception {
        PageResult<ExamItemVO> page = new PageResult<>(0L, 1L, 20L, Collections.emptyList());
        when(selfTestService.listPendingExams(eq(USER_ID), anyLong(), anyLong())).thenReturn(page);

        mockMvc.perform(get("/api/v1/self-test/exams/pending")
                        .header(USER_HEADER, USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(0));
    }

    @Test
    @DisplayName("GET /exams/finished - 正常返回已考列表")
    void finishedExams_success() throws Exception {
        PageResult<ExamItemVO> page = new PageResult<>(1L, 1L, 20L,
                Collections.singletonList(ExamItemVO.builder()
                        .examId(1001L).gradeId(100L)
                        .userScore(new BigDecimal("85")).isPassed(1).wrongCount(2)
                        .build()));
        when(selfTestService.listFinishedExams(eq(USER_ID), anyLong(), anyLong())).thenReturn(page);

        mockMvc.perform(get("/api/v1/self-test/exams/finished")
                        .header(USER_HEADER, USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.records[0].gradeId").value(100))
                .andExpect(jsonPath("$.data.records[0].userScore").value(85))
                .andExpect(jsonPath("$.data.records[0].wrongCount").value(2));
    }

    @Test
    @DisplayName("GET /wrong-questions - 默认参数")
    void wrongQuestions_defaults() throws Exception {
        PageResult<WrongQuestionVO> page = new PageResult<>(0L, 1L, 20L, Collections.emptyList());
        when(selfTestService.listWrongQuestions(eq(USER_ID), any(), anyLong(), anyLong())).thenReturn(page);

        mockMvc.perform(get("/api/v1/self-test/wrong-questions")
                        .header(USER_HEADER, USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.pageNum").value(1));
    }

    @Test
    @DisplayName("GET /wrong-questions?isMastered=0 - 仅未掌握")
    void wrongQuestions_filterUnmastered() throws Exception {
        PageResult<WrongQuestionVO> page = new PageResult<>(0L, 1L, 20L, Collections.emptyList());
        when(selfTestService.listWrongQuestions(eq(USER_ID), eq(0), anyLong(), anyLong())).thenReturn(page);

        mockMvc.perform(get("/api/v1/self-test/wrong-questions")
                        .header(USER_HEADER, USER_ID)
                        .param("isMastered", "0"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /wrong-questions/{id} - 详情")
    void wrongQuestionDetail_success() throws Exception {
        WrongQuestionVO vo = WrongQuestionVO.builder()
                .id(1L).questionId(100L).aiExplanation("已解析").build();
        when(selfTestService.getWrongQuestion(eq(1L), eq(USER_ID))).thenReturn(vo);

        mockMvc.perform(get("/api/v1/self-test/wrong-questions/{id}", 1)
                        .header(USER_HEADER, USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.aiExplanation").value("已解析"));
    }

    @Test
    @DisplayName("POST /wrong-questions/{id}/ai - 触发 AI")
    void triggerAi_success() throws Exception {
        WrongQuestionVO vo = WrongQuestionVO.builder().id(1L)
                .aiExplanation("AI 解析成功").build();
        when(selfTestService.triggerAiExplanation(eq(1L), eq(USER_ID))).thenReturn(vo);

        mockMvc.perform(post("/api/v1/self-test/wrong-questions/{id}/ai", 1)
                        .header(USER_HEADER, USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.aiExplanation").value("AI 解析成功"));
    }

    @Test
    @DisplayName("POST /wrong-questions/{id}/ai - AI 失败：业务码 AI_INVOKE_FAIL")
    void triggerAi_fail() throws Exception {
        when(selfTestService.triggerAiExplanation(eq(1L), eq(USER_ID)))
                .thenThrow(new BusinessException(ErrorCode.AI_INVOKE_FAIL));

        mockMvc.perform(post("/api/v1/self-test/wrong-questions/{id}/ai", 1)
                        .header(USER_HEADER, USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.AI_INVOKE_FAIL.getCode()));
    }

    @Test
    @DisplayName("POST /wrong-questions/{id}/master - 标记掌握")
    void markMastered_success() throws Exception {
        mockMvc.perform(post("/api/v1/self-test/wrong-questions/{id}/master", 1)
                        .header(USER_HEADER, USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }
}
