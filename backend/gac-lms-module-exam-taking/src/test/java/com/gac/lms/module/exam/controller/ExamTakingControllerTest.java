package com.gac.lms.module.exam.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gac.lms.common.exception.BusinessException;
import com.gac.lms.common.exception.GlobalExceptionHandler;
import com.gac.lms.common.response.ErrorCode;
import com.gac.lms.module.exam.dto.SaveAnswerRequest;
import com.gac.lms.module.exam.service.ExamTakingService;
import com.gac.lms.module.exam.vo.PaperRenderVO;
import com.gac.lms.module.exam.vo.RemainingTimeVO;
import com.gac.lms.module.exam.vo.SubmitResultVO;
import com.gac.lms.module.exam.vo.TakingSnapshotVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ExamTakingController 单元测试。
 *
 * <p>使用 {@link MockMvcBuilders#standaloneSetup} 模式，不加载 Spring Security 等
 * 自动配置，专注于 Controller 本身的路由 + 参数校验 + 异常处理。</p>
 *
 * @author 方雨菲
 */
class ExamTakingControllerTest {

    private MockMvc mockMvc;
    private ExamTakingService examTakingService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final Long EXAM_ID = 1001L;
    private static final Long USER_ID = 8888L;
    private static final String USER_ID_HEADER = "X-User-Id";

    @BeforeEach
    void setUp() {
        examTakingService = mock(ExamTakingService.class);
        ExamTakingController controller = new ExamTakingController(examTakingService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /health - 返回 ok")
    void health_ok() throws Exception {
        mockMvc.perform(get("/api/v1/exam-taking/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value("exam-taking-module-ok"));
    }

    @Test
    @DisplayName("GET /{examId}/paper - 正常返回试卷")
    void renderPaper_success() throws Exception {
        PaperRenderVO paper = new PaperRenderVO();
        paper.setExamId(EXAM_ID);
        paper.setPaperId(2001L);
        paper.setTitle("Mock Paper");
        paper.setDurationMinutes(60);
        paper.setTotalScore(100);
        paper.setCurrentVersion(0);
        when(examTakingService.renderPaper(EXAM_ID, USER_ID)).thenReturn(paper);

        mockMvc.perform(get("/api/v1/exam-taking/{examId}/paper", EXAM_ID)
                        .header(USER_ID_HEADER, USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.paperId").value(2001))
                .andExpect(jsonPath("$.data.currentVersion").value(0));
    }

    @Test
    @DisplayName("GET /{examId}/paper - 缺少 X-User-Id：业务码 UNAUTHORIZED")
    void renderPaper_missingUserHeader_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/exam-taking/{examId}/paper", EXAM_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.UNAUTHORIZED.getCode()));
    }

    @Test
    @DisplayName("POST /{examId}/save - 正常暂存")
    void saveAnswer_success() throws Exception {
        SaveAnswerRequest req = new SaveAnswerRequest();
        req.setQuestionId(10L);
        req.setAnswer("A");
        req.setVersion(3);
        when(examTakingService.saveAnswer(eq(EXAM_ID), eq(USER_ID), any())).thenReturn(4);

        mockMvc.perform(post("/api/v1/exam-taking/{examId}/save", EXAM_ID)
                        .header(USER_ID_HEADER, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value(4));
    }

    @Test
    @DisplayName("POST /{examId}/save - 参数校验失败（version 缺失）")
    void saveAnswer_validationFails() throws Exception {
        // version 字段缺失 → @NotNull 校验失败
        String body = "{\"questionId\":10,\"answer\":\"A\"}";

        mockMvc.perform(post("/api/v1/exam-taking/{examId}/save", EXAM_ID)
                        .header(USER_ID_HEADER, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.BAD_REQUEST.getCode()));
    }

    @Test
    @DisplayName("POST /{examId}/save - 版本冲突：返回 ANSWER_SAVE_CONFLICT")
    void saveAnswer_versionConflict() throws Exception {
        SaveAnswerRequest req = new SaveAnswerRequest();
        req.setQuestionId(10L);
        req.setAnswer("A");
        req.setVersion(3);
        when(examTakingService.saveAnswer(eq(EXAM_ID), eq(USER_ID), any()))
                .thenThrow(new BusinessException(ErrorCode.ANSWER_SAVE_CONFLICT));

        mockMvc.perform(post("/api/v1/exam-taking/{examId}/save", EXAM_ID)
                        .header(USER_ID_HEADER, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.ANSWER_SAVE_CONFLICT.getCode()))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("答题版本冲突")));
    }

    @Test
    @DisplayName("POST /{examId}/submit - 正常交卷")
    void submit_success() throws Exception {
        SubmitResultVO result = SubmitResultVO.builder()
                .takingId(100L)
                .examId(EXAM_ID)
                .submitTime(LocalDateTime.now())
                .answeredCount(5)
                .message("交卷成功")
                .build();
        when(examTakingService.submit(EXAM_ID, USER_ID)).thenReturn(result);

        mockMvc.perform(post("/api/v1/exam-taking/{examId}/submit", EXAM_ID)
                        .header(USER_ID_HEADER, USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.takingId").value(100))
                .andExpect(jsonPath("$.data.answeredCount").value(5));
    }

    @Test
    @DisplayName("GET /{examId}/snapshot - 正常返回快照")
    void getSnapshot_success() throws Exception {
        when(examTakingService.getSnapshot(EXAM_ID, USER_ID)).thenReturn(
                TakingSnapshotVO.builder()
                        .examId(EXAM_ID)
                        .paperId(2001L)
                        .version(5)
                        .answeredCount(3)
                        .totalCount(5)
                        .build());

        mockMvc.perform(get("/api/v1/exam-taking/{examId}/snapshot", EXAM_ID)
                        .header(USER_ID_HEADER, USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.version").value(5))
                .andExpect(jsonPath("$.data.answeredCount").value(3));
    }

    @Test
    @DisplayName("GET /{examId}/remaining - 正常返回剩余时间")
    void getRemaining_success() throws Exception {
        when(examTakingService.getRemaining(EXAM_ID, USER_ID)).thenReturn(
                new RemainingTimeVO(3000L, false, System.currentTimeMillis()));

        mockMvc.perform(get("/api/v1/exam-taking/{examId}/remaining", EXAM_ID)
                        .header(USER_ID_HEADER, USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.remainingSeconds").value(3000))
                .andExpect(jsonPath("$.data.overtime").value(false));
    }
}
