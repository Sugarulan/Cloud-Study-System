package com.gac.lms.module.integration.service;

import com.gac.lms.common.response.PageResult;
import com.gac.lms.module.integration.dto.AiExtractRequest;
import com.gac.lms.module.integration.dto.EmailSendRequest;
import com.gac.lms.module.integration.dto.MessagePushRequest;
import com.gac.lms.module.integration.dto.WebhookCreateRequest;
import com.gac.lms.module.integration.vo.EmailSendResultVO;
import com.gac.lms.module.integration.vo.ExtractedQuestionVO;
import com.gac.lms.module.integration.vo.MessageVO;
import com.gac.lms.module.integration.vo.WebhookConfigVO;
import com.gac.lms.module.integration.vo.WebhookTestResultVO;

/**
 * 系统集成 Service。
 *
 * <p>W2 简化策略：</p>
 * <ul>
 *   <li>邮件：Mock 发送（写日志表 + DEBUG 日志，不真正连 SMTP）</li>
 *   <li>站内信：直接写 sys_message 表</li>
 *   <li>Webhook：RestTemplate 异步推送（W2 同步调用，W5 改异步）</li>
 *   <li>AI 抽题：调用 LLMService（Mock），解析返回结构化题目</li>
 * </ul>
 *
 * @author 方雨菲
 */
public interface IntegrationService {

    // ========== 邮件 ==========

    /**
     * 发送邮件（直接发送 / 模板发送）。
     */
    EmailSendResultVO sendEmail(EmailSendRequest request);

    // ========== 站内信 ==========

    /**
     * 推送一条站内信。
     */
    MessageVO pushMessage(MessagePushRequest request);

    /**
     * 当前用户收件箱。
     */
    PageResult<MessageVO> listMessages(Long userId, Integer isRead, int pageNum, int pageSize);

    /**
     * 未读数。
     */
    long unreadCount(Long userId);

    /**
     * 标记已读。
     */
    void markAsRead(Long id, Long userId);

    // ========== Webhook ==========

    /**
     * 配置列表。
     */
    PageResult<WebhookConfigVO> listWebhooks(int pageNum, int pageSize);

    /**
     * 创建配置。
     */
    WebhookConfigVO createWebhook(WebhookCreateRequest request, Long operatorId);

    /**
     * 测试推送。
     */
    WebhookTestResultVO testWebhook(Long id);

    // ========== AI 抽题 ==========

    /**
     * AI 文档 → 题目抽取。
     */
    ExtractedQuestionVO aiExtractQuestions(AiExtractRequest request);
}
