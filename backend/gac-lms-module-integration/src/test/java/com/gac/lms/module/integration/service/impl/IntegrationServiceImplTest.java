package com.gac.lms.module.integration.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gac.lms.ai.service.LLMService;
import com.gac.lms.common.exception.BusinessException;
import com.gac.lms.common.response.ErrorCode;
import com.gac.lms.common.response.PageResult;
import com.gac.lms.module.integration.dto.AiExtractRequest;
import com.gac.lms.module.integration.dto.EmailSendRequest;
import com.gac.lms.module.integration.dto.MessagePushRequest;
import com.gac.lms.module.integration.dto.WebhookCreateRequest;
import com.gac.lms.module.integration.entity.SysMessage;
import com.gac.lms.module.integration.entity.WebhookConfig;
import com.gac.lms.module.integration.mapper.EmailLogMapper;
import com.gac.lms.module.integration.mapper.SysMessageMapper;
import com.gac.lms.module.integration.mapper.WebhookConfigMapper;
import com.gac.lms.module.integration.vo.EmailSendResultVO;
import com.gac.lms.module.integration.vo.ExtractedQuestionVO;
import com.gac.lms.module.integration.vo.MessageVO;
import com.gac.lms.module.integration.vo.WebhookConfigVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * IntegrationServiceImpl 单元测试。
 *
 * @author 方雨菲
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class IntegrationServiceImplTest {

    @Mock private EmailLogMapper emailLogMapper;
    @Mock private SysMessageMapper sysMessageMapper;
    @Mock private WebhookConfigMapper webhookConfigMapper;
    @Mock private LLMService llmService;

    private IntegrationServiceImpl service;

    private static final Long USER_ID = 100L;

    @BeforeEach
    void setUp() {
        service = new IntegrationServiceImpl(emailLogMapper, sysMessageMapper,
                webhookConfigMapper, llmService);
    }

    // ========== 邮件 ==========

    @Test
    @DisplayName("sendEmail - 直接发送：每个收件人写一条日志")
    void sendEmail_direct() {
        when(emailLogMapper.insert(any(com.gac.lms.module.integration.entity.EmailLog.class)))
                .thenAnswer(inv -> {
                    com.gac.lms.module.integration.entity.EmailLog log = inv.getArgument(0);
                    log.setId(System.nanoTime());
                    return 1;
                });

        EmailSendRequest req = new EmailSendRequest();
        req.setTo(Arrays.asList("a@gac.local", "b@gac.local"));
        req.setSubject("测试");
        req.setContent("正文");

        EmailSendResultVO result = service.sendEmail(req);

        assertThat(result.getSuccessCount()).isEqualTo(2);
        assertThat(result.getFailedCount()).isEqualTo(0);
        assertThat(result.getLogId()).isNotNull();
    }

    @Test
    @DisplayName("sendEmail - 模板发送：使用 EXAM_REMIND 模板渲染")
    void sendEmail_template() {
        EmailSendRequest req = new EmailSendRequest();
        req.setTo(Collections.singletonList("user@gac.local"));
        req.setTemplateCode("EXAM_REMIND");
        Map<String, String> params = new HashMap<>();
        params.put("examName", "Java 考试");
        params.put("userName", "张三");
        params.put("startTime", "2026-07-15 14:00");
        params.put("duration", "60");
        req.setParams(params);

        EmailSendResultVO result = service.sendEmail(req);

        assertThat(result.getSuccessCount()).isEqualTo(1);
        assertThat(result.getTemplateCode()).isEqualTo("EXAM_REMIND");
    }

    // ========== 站内信 ==========

    @Test
    @DisplayName("pushMessage - 正常插入并返回 VO")
    void pushMessage_success() {
        MessagePushRequest req = new MessagePushRequest();
        req.setUserId(USER_ID);
        req.setType("EXAM_REMIND");
        req.setTitle("考试提醒");
        req.setContent("明天 14:00 开始");

        MessageVO vo = service.pushMessage(req);

        assertThat(vo.getUserId()).isEqualTo(USER_ID);
        assertThat(vo.getType()).isEqualTo("EXAM_REMIND");
        assertThat(vo.getIsRead()).isEqualTo(0);
    }

    @Test
    @DisplayName("listMessages - 按 userId + isRead 筛选")
    void listMessages_filter() {
        when(sysMessageMapper.selectCount(any(QueryWrapper.class))).thenReturn(2L);

        SysMessage m1 = new SysMessage();
        m1.setId(1L);
        m1.setUserId(USER_ID);
        m1.setType("EXAM_REMIND");
        m1.setIsRead(0);
        SysMessage m2 = new SysMessage();
        m2.setId(2L);
        m2.setUserId(USER_ID);
        m2.setType("GRADE_PUBLISH");
        m2.setIsRead(0);
        when(sysMessageMapper.selectList(any(QueryWrapper.class)))
                .thenReturn(Arrays.asList(m1, m2));

        PageResult<MessageVO> page = service.listMessages(USER_ID, 0, 1, 20);

        assertThat(page.getTotal()).isEqualTo(2L);
        assertThat(page.getRecords()).hasSize(2);
    }

    @Test
    @DisplayName("unreadCount - 返回未读数")
    void unreadCount() {
        when(sysMessageMapper.selectCount(any(QueryWrapper.class))).thenReturn(5L);

        long count = service.unreadCount(USER_ID);

        assertThat(count).isEqualTo(5L);
    }

    @Test
    @DisplayName("markAsRead - 正常：标记成功")
    void markAsRead_success() {
        SysMessage m = new SysMessage();
        m.setId(1L);
        m.setUserId(USER_ID);
        m.setIsRead(0);
        when(sysMessageMapper.selectById(1L)).thenReturn(m);

        service.markAsRead(1L, USER_ID);

        assertThat(m.getIsRead()).isEqualTo(1);
        assertThat(m.getReadTime()).isNotNull();
    }

    @Test
    @DisplayName("markAsRead - 越权：抛 FORBIDDEN")
    void markAsRead_otherUser() {
        SysMessage m = new SysMessage();
        m.setId(1L);
        m.setUserId(9999L);
        when(sysMessageMapper.selectById(1L)).thenReturn(m);

        assertThatThrownBy(() -> service.markAsRead(1L, USER_ID))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("无权");
    }

    // ========== Webhook ==========

    @Test
    @DisplayName("createWebhook - 正常创建")
    void createWebhook_success() {
        WebhookCreateRequest req = new WebhookCreateRequest();
        req.setName("测试 Webhook");
        req.setUrl("http://example.com/hook");
        req.setEvents("EXAM_PUBLISH");
        req.setSecret("secret-123");

        WebhookConfigVO vo = service.createWebhook(req, USER_ID);

        assertThat(vo.getName()).isEqualTo("测试 Webhook");
        assertThat(vo.getHasSecret()).isTrue();
        assertThat(vo.getStatus()).isEqualTo(1);
    }

    @Test
    @DisplayName("listWebhooks - 返回列表（secret 不暴露明文）")
    void listWebhooks_secretHidden() {
        when(webhookConfigMapper.selectCount(any(QueryWrapper.class))).thenReturn(1L);
        WebhookConfig c = new WebhookConfig();
        c.setId(1L);
        c.setName("测试");
        c.setUrl("http://example.com");
        c.setSecret("super-secret");
        c.setStatus(1);
        when(webhookConfigMapper.selectList(any(QueryWrapper.class)))
                .thenReturn(Collections.singletonList(c));

        PageResult<WebhookConfigVO> page = service.listWebhooks(1, 20);

        assertThat(page.getRecords()).hasSize(1);
        assertThat(page.getRecords().get(0).getHasSecret()).isTrue();
    }

    @Test
    @DisplayName("testWebhook - 配置不存在：抛 DATA_NOT_FOUND")
    void testWebhook_notFound() {
        when(webhookConfigMapper.selectById(999L)).thenReturn(null);

        assertThatThrownBy(() -> service.testWebhook(999L))
                .isInstanceOf(BusinessException.class);
    }

    // ========== AI 抽题 ==========

    @Test
    @DisplayName("aiExtractQuestions - Mock 返回有效 JSON：解析题目列表")
    void aiExtract_validJson() {
        when(llmService.invoke(any(String.class))).thenReturn(
                "[{\"type\":\"SINGLE\",\"stem\":\"示例题\",\"correctAnswer\":\"A\",\"difficulty\":\"EASY\"}]");

        AiExtractRequest req = new AiExtractRequest();
        req.setDocId(100L);
        req.setExpectedCount(3);

        ExtractedQuestionVO result = service.aiExtractQuestions(req);

        assertThat(result.getDocId()).isEqualTo(100L);
        assertThat(result.getQuestions()).hasSize(1);
        assertThat(result.getQuestions().get(0).getType()).isEqualTo("SINGLE");
    }

    @Test
    @DisplayName("aiExtractQuestions - Mock 返回空：兜底返回占位题")
    void aiExtract_empty_fallback() {
        when(llmService.invoke(any(String.class))).thenReturn("");

        AiExtractRequest req = new AiExtractRequest();
        req.setDocId(100L);

        ExtractedQuestionVO result = service.aiExtractQuestions(req);

        assertThat(result.getQuestions()).hasSize(1);
        assertThat(result.getQuestions().get(0).getStem()).contains("暂未返回");
    }

    @Test
    @DisplayName("aiExtractQuestions - AI 抛业务异常：转 AI_INVOKE_FAIL")
    void aiExtract_propagateBusinessException() {
        when(llmService.invoke(any(String.class)))
                .thenThrow(new BusinessException(ErrorCode.AI_INVOKE_FAIL));

        AiExtractRequest req = new AiExtractRequest();
        req.setDocId(100L);

        assertThatThrownBy(() -> service.aiExtractQuestions(req))
                .isInstanceOf(BusinessException.class);
    }
}
