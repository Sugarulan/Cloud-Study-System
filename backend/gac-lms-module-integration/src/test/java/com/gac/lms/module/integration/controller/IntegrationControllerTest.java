package com.gac.lms.module.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gac.lms.common.exception.GlobalExceptionHandler;
import com.gac.lms.common.response.ErrorCode;
import com.gac.lms.common.response.PageResult;
import com.gac.lms.module.integration.dto.EmailSendRequest;
import com.gac.lms.module.integration.dto.MessagePushRequest;
import com.gac.lms.module.integration.dto.WebhookCreateRequest;
import com.gac.lms.module.integration.service.IntegrationService;
import com.gac.lms.module.integration.vo.EmailSendResultVO;
import com.gac.lms.module.integration.vo.MessageVO;
import com.gac.lms.module.integration.vo.WebhookConfigVO;
import com.gac.lms.module.integration.vo.WebhookTestResultVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

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
 * IntegrationController 单元测试。
 *
 * @author 方雨菲
 */
class IntegrationControllerTest {

    private MockMvc mockMvc;
    private IntegrationService integrationService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final Long USER_ID = 100L;
    private static final String USER_HEADER = "X-User-Id";

    @BeforeEach
    void setUp() {
        integrationService = mock(IntegrationService.class);
        IntegrationController controller = new IntegrationController(integrationService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /health")
    void health_ok() throws Exception {
        mockMvc.perform(get("/api/v1/integration/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value("integration-module-ok"));
    }

    @Test
    @DisplayName("POST /email/send - 直接发送")
    void sendEmail_direct() throws Exception {
        EmailSendResultVO vo = EmailSendResultVO.builder()
                .successCount(2).failedCount(0).build();
        when(integrationService.sendEmail(any(EmailSendRequest.class))).thenReturn(vo);

        EmailSendRequest req = new EmailSendRequest();
        req.setTo(java.util.Arrays.asList("a@gac.local", "b@gac.local"));
        req.setSubject("测试");
        req.setContent("正文");

        mockMvc.perform(post("/api/v1/integration/email/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.successCount").value(2));
    }

    @Test
    @DisplayName("POST /email/send - 缺收件人：参数校验失败")
    void sendEmail_validationFail() throws Exception {
        EmailSendRequest req = new EmailSendRequest();
        // 缺 to

        mockMvc.perform(post("/api/v1/integration/email/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.BAD_REQUEST.getCode()));
    }

    @Test
    @DisplayName("POST /email/template - 缺 templateCode：业务码 BAD_REQUEST")
    void sendTemplate_missingCode() throws Exception {
        EmailSendRequest req = new EmailSendRequest();
        req.setTo(Collections.singletonList("a@gac.local"));
        // 缺 templateCode

        mockMvc.perform(post("/api/v1/integration/email/template")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.BAD_REQUEST.getCode()));
    }

    @Test
    @DisplayName("POST /message/push - 推送站内信")
    void pushMessage_success() throws Exception {
        MessageVO vo = MessageVO.builder().id(1L).userId(USER_ID).type("EXAM_REMIND").build();
        when(integrationService.pushMessage(any(MessagePushRequest.class))).thenReturn(vo);

        MessagePushRequest req = new MessagePushRequest();
        req.setUserId(USER_ID);
        req.setType("EXAM_REMIND");
        req.setTitle("提醒");
        req.setContent("内容");

        mockMvc.perform(post("/api/v1/integration/message/push")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @DisplayName("GET /message/list - 缺 X-User-Id：UNAUTHORIZED")
    void listMessages_missingUser() throws Exception {
        mockMvc.perform(get("/api/v1/integration/message/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.UNAUTHORIZED.getCode()));
    }

    @Test
    @DisplayName("GET /message/unread-count - 返回未读数")
    void unreadCount() throws Exception {
        when(integrationService.unreadCount(eq(USER_ID))).thenReturn(5L);

        mockMvc.perform(get("/api/v1/integration/message/unread-count")
                        .header(USER_HEADER, USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.unreadCount").value(5));
    }

    @Test
    @DisplayName("POST /message/{id}/read - 标记已读")
    void markAsRead() throws Exception {
        mockMvc.perform(post("/api/v1/integration/message/{id}/read", 1)
                        .header(USER_HEADER, USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    @DisplayName("GET /webhooks - 列表")
    void listWebhooks() throws Exception {
        PageResult<WebhookConfigVO> page = new PageResult<>(0L, 1L, 20L, Collections.emptyList());
        when(integrationService.listWebhooks(any(Integer.class), any(Integer.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/integration/webhooks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    @DisplayName("POST /webhooks - 创建 Webhook")
    void createWebhook() throws Exception {
        WebhookConfigVO vo = WebhookConfigVO.builder().id(1L).name("测试").build();
        when(integrationService.createWebhook(any(WebhookCreateRequest.class), eq(USER_ID)))
                .thenReturn(vo);

        WebhookCreateRequest req = new WebhookCreateRequest();
        req.setName("测试");
        req.setUrl("http://example.com/hook");

        mockMvc.perform(post("/api/v1/integration/webhooks")
                        .header(USER_HEADER, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @DisplayName("POST /webhooks/{id}/test - 测试推送")
    void testWebhook() throws Exception {
        WebhookTestResultVO result = WebhookTestResultVO.builder()
                .webhookId(1L).success(true).httpStatus(200).costMs(120L).build();
        when(integrationService.testWebhook(eq(1L))).thenReturn(result);

        mockMvc.perform(post("/api/v1/integration/webhooks/{id}/test", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.success").value(true))
                .andExpect(jsonPath("$.data.httpStatus").value(200));
    }

    @Test
    @DisplayName("POST /ai/extract - AI 抽题")
    void aiExtract() throws Exception {
        com.gac.lms.module.integration.vo.ExtractedQuestionVO vo =
                com.gac.lms.module.integration.vo.ExtractedQuestionVO.builder()
                        .docId(100L).questions(Collections.emptyList()).build();
        when(integrationService.aiExtractQuestions(any())).thenReturn(vo);

        String body = "{\"docId\":100,\"expectedCount\":3}";

        mockMvc.perform(post("/api/v1/integration/ai/extract")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.docId").value(100));
    }
}
