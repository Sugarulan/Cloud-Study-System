package com.gac.lms.module.evaluation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gac.lms.common.exception.BusinessException;
import com.gac.lms.common.exception.GlobalExceptionHandler;
import com.gac.lms.common.response.ErrorCode;
import com.gac.lms.common.response.PageResult;
import com.gac.lms.module.evaluation.dto.AiEvaluateRequest;
import com.gac.lms.module.evaluation.dto.AutoEvaluateRequest;
import com.gac.lms.module.evaluation.service.EvaluationService;
import com.gac.lms.module.evaluation.vo.EvaluationActionVO;
import com.gac.lms.module.evaluation.vo.EvaluationResultVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * EvaluationController 单元测试。
 *
 * @author 方雨菲
 */
class EvaluationControllerTest {

    private MockMvc mockMvc;
    private EvaluationService evaluationService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final Long GRADE_ID = 100L;
    private static final Long OPERATOR_ID = 999L;
    private static final String USER_HEADER = "X-User-Id";

    @BeforeEach
    void setUp() {
        evaluationService = mock(EvaluationService.class);
        EvaluationController controller = new EvaluationController(evaluationService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /health - 返回 ok")
    void health_ok() throws Exception {
        mockMvc.perform(get("/api/v1/evaluation/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value("evaluation-module-ok"));
    }

    @Test
    @DisplayName("POST /auto - 自动评阅成功")
    void autoEvaluate_success() throws Exception {
        EvaluationResultVO result = EvaluationResultVO.builder()
                .gradeId(1L).examId(1001L).userId(100L)
                .totalScore(new BigDecimal("85"))
                .objectiveScore(new BigDecimal("85"))
                .status(2)
                .isPassed(1)
                .details(Collections.emptyList())
                .build();
        when(evaluationService.autoEvaluate(any())).thenReturn(result);

        AutoEvaluateRequest req = new AutoEvaluateRequest();
        req.setExamId(1001L);
        req.setUserId(100L);
        AutoEvaluateRequest.QuestionAnswer qa = new AutoEvaluateRequest.QuestionAnswer();
        qa.setQuestionId(1L);
        qa.setType("SINGLE");
        qa.setUserAnswer("A");
        qa.setCorrectAnswer("A");
        qa.setFullScore(20);
        req.setAnswers(Arrays.asList(qa));

        mockMvc.perform(post("/api/v1/evaluation/auto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.gradeId").value(1))
                .andExpect(jsonPath("$.data.totalScore").value(85));
    }

    @Test
    @DisplayName("POST /auto - 参数校验失败")
    void autoEvaluate_validationFails() throws Exception {
        // 缺 examId
        AutoEvaluateRequest req = new AutoEvaluateRequest();
        req.setUserId(100L);

        mockMvc.perform(post("/api/v1/evaluation/auto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.BAD_REQUEST.getCode()));
    }

    @Test
    @DisplayName("POST /ai - AI 评阅成功")
    void aiEvaluate_success() throws Exception {
        EvaluationResultVO result = EvaluationResultVO.builder()
                .gradeId(1L).examId(1001L).userId(100L)
                .subjectiveScore(new BigDecimal("15"))
                .status(2)
                .build();
        when(evaluationService.aiEvaluate(any())).thenReturn(result);

        AiEvaluateRequest req = new AiEvaluateRequest();
        req.setExamId(1001L);
        req.setUserId(100L);
        AiEvaluateRequest.EssayAnswer ea = new AiEvaluateRequest.EssayAnswer();
        ea.setQuestionId(5L);
        ea.setStem("题");
        ea.setUserAnswer("答");
        ea.setFullScore(20);
        req.setEssays(Arrays.asList(ea));

        mockMvc.perform(post("/api/v1/evaluation/ai")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.subjectiveScore").value(15));
    }

    @Test
    @DisplayName("POST /ai - AI 失败：业务码 EVALUATION_AI_FAIL")
    void aiEvaluate_aiFail() throws Exception {
        when(evaluationService.aiEvaluate(any()))
                .thenThrow(new BusinessException(ErrorCode.EVALUATION_AI_FAIL));

        AiEvaluateRequest req = new AiEvaluateRequest();
        req.setExamId(1001L);
        req.setUserId(100L);
        AiEvaluateRequest.EssayAnswer ea = new AiEvaluateRequest.EssayAnswer();
        ea.setQuestionId(5L);
        ea.setStem("题");
        ea.setUserAnswer("答");
        ea.setFullScore(20);
        req.setEssays(Arrays.asList(ea));

        mockMvc.perform(post("/api/v1/evaluation/ai")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.EVALUATION_AI_FAIL.getCode()));
    }

    @Test
    @DisplayName("POST /manual - 人工评阅成功")
    void manualEvaluate_success() throws Exception {
        EvaluationResultVO result = EvaluationResultVO.builder()
                .gradeId(GRADE_ID).totalScore(new BigDecimal("70")).status(2).build();
        when(evaluationService.manualEvaluate(any(), eq(OPERATOR_ID))).thenReturn(result);

        String body = "{\"gradeId\":100,\"items\":[{\"questionId\":1,\"score\":15}]}";

        mockMvc.perform(post("/api/v1/evaluation/manual")
                        .header(USER_HEADER, OPERATOR_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.gradeId").value(100));
    }

    @Test
    @DisplayName("POST /manual - 缺 X-User-Id：业务码 UNAUTHORIZED")
    void manualEvaluate_missingUserHeader() throws Exception {
        String body = "{\"gradeId\":100,\"items\":[{\"questionId\":1,\"score\":15}]}";

        mockMvc.perform(post("/api/v1/evaluation/manual")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.UNAUTHORIZED.getCode()));
    }

    @Test
    @DisplayName("POST /{id}/review - 复核成功")
    void review_success() throws Exception {
        EvaluationActionVO vo = EvaluationActionVO.builder()
                .gradeId(GRADE_ID).status(3).message("复核完成").build();
        when(evaluationService.review(eq(GRADE_ID), eq(OPERATOR_ID))).thenReturn(vo);

        mockMvc.perform(post("/api/v1/evaluation/{id}/review", GRADE_ID)
                        .header(USER_HEADER, OPERATOR_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value(3));
    }

    @Test
    @DisplayName("POST /{id}/publish - 发布成功")
    void publish_success() throws Exception {
        EvaluationActionVO vo = EvaluationActionVO.builder()
                .gradeId(GRADE_ID).status(4).message("成绩已发布").build();
        when(evaluationService.publish(eq(GRADE_ID), eq(OPERATOR_ID))).thenReturn(vo);

        mockMvc.perform(post("/api/v1/evaluation/{id}/publish", GRADE_ID)
                        .header(USER_HEADER, OPERATOR_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value(4));
    }

    @Test
    @DisplayName("GET /pending - 待评列表")
    void listPending_success() throws Exception {
        PageResult<com.gac.lms.module.evaluation.vo.PendingItemVO> page =
                new PageResult<>(1L, 1, 20, Collections.emptyList());
        when(evaluationService.listPending(any(), any(), eq(1), eq(20))).thenReturn(page);

        mockMvc.perform(get("/api/v1/evaluation/pending")
                        .param("pageNum", "1")
                        .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    @DisplayName("GET /{id} - 成绩详情")
    void getDetail_success() throws Exception {
        EvaluationResultVO result = EvaluationResultVO.builder()
                .gradeId(GRADE_ID).totalScore(new BigDecimal("85")).build();
        when(evaluationService.getDetail(eq(GRADE_ID))).thenReturn(result);

        mockMvc.perform(get("/api/v1/evaluation/{id}", GRADE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.gradeId").value(100));
    }
}
