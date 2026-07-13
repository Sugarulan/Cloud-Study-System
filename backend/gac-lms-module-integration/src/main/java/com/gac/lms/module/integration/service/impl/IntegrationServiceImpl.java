package com.gac.lms.module.integration.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.gac.lms.ai.service.LLMService;
import com.gac.lms.common.exception.BusinessException;
import com.gac.lms.common.response.ErrorCode;
import com.gac.lms.common.response.PageResult;
import com.gac.lms.module.integration.dto.AiExtractRequest;
import com.gac.lms.module.integration.dto.EmailSendRequest;
import com.gac.lms.module.integration.dto.MessagePushRequest;
import com.gac.lms.module.integration.dto.WebhookCreateRequest;
import com.gac.lms.module.integration.entity.EmailLog;
import com.gac.lms.module.integration.entity.SysMessage;
import com.gac.lms.module.integration.entity.WebhookConfig;
import com.gac.lms.module.integration.mapper.EmailLogMapper;
import com.gac.lms.module.integration.mapper.SysMessageMapper;
import com.gac.lms.module.integration.mapper.WebhookConfigMapper;
import com.gac.lms.module.integration.service.EmailTemplateRegistry;
import com.gac.lms.module.integration.service.IntegrationService;
import com.gac.lms.module.integration.vo.EmailSendResultVO;
import com.gac.lms.module.integration.vo.ExtractedQuestionVO;
import com.gac.lms.module.integration.vo.MessageVO;
import com.gac.lms.module.integration.vo.WebhookConfigVO;
import com.gac.lms.module.integration.vo.WebhookTestResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统集成 Service 实现。
 *
 * @author 方雨菲
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IntegrationServiceImpl implements IntegrationService {

    public static final int EMAIL_STATUS_PENDING = 0;
    public static final int EMAIL_STATUS_SUCCESS = 1;
    public static final int EMAIL_STATUS_FAILED = 2;

    public static final int WEBHOOK_ENABLED = 1;

    private final EmailLogMapper emailLogMapper;
    private final SysMessageMapper sysMessageMapper;
    private final WebhookConfigMapper webhookConfigMapper;
    private final LLMService llmService;

    private final RestTemplate restTemplate = new RestTemplate();

    // ========== 邮件 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EmailSendResultVO sendEmail(EmailSendRequest request) {
        log.info("[sendEmail] to={} subject={} template={}",
                request.getTo(), request.getSubject(), request.getTemplateCode());

        // 模板渲染
        String subject;
        String content;
        if (request.getTemplateCode() != null && !request.getTemplateCode().isBlank()) {
            String[] rendered = EmailTemplateRegistry.render(request.getTemplateCode(), request.getParams());
            subject = rendered[0];
            content = rendered[1];
        } else {
            subject = request.getSubject();
            content = request.getContent();
        }

        List<String> failed = new ArrayList<>();
        int successCount = 0;
        EmailLog lastLog = null;

        // W2 简化：逐个收件人写日志（不真正发）
        for (String toEmail : request.getTo()) {
            EmailLog log = new EmailLog();
            log.setTemplateCode(request.getTemplateCode());
            log.setToEmail(toEmail);
            log.setSubject(subject);
            log.setContent(content);
            log.setStatus(EMAIL_STATUS_SUCCESS); // W2 假定成功
            emailLogMapper.insert(log);
            successCount++;
            lastLog = log;
        }

        return EmailSendResultVO.builder()
                .logId(lastLog == null ? null : lastLog.getId())
                .successCount(successCount)
                .failedCount(failed.size())
                .failedRecipients(failed)
                .templateCode(request.getTemplateCode())
                .build();
    }

    // ========== 站内信 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MessageVO pushMessage(MessagePushRequest request) {
        log.info("[pushMessage] userId={} type={} title={}",
                request.getUserId(), request.getType(), request.getTitle());

        SysMessage msg = new SysMessage();
        msg.setUserId(request.getUserId());
        msg.setType(request.getType());
        msg.setTitle(request.getTitle());
        msg.setContent(request.getContent());
        msg.setIsRead(0);
        msg.setCreateBy(0L); // 系统消息
        sysMessageMapper.insert(msg);

        return toMessageVO(msg);
    }

    @Override
    public PageResult<MessageVO> listMessages(Long userId, Integer isRead, int pageNum, int pageSize) {
        QueryWrapper<SysMessage> qw = new QueryWrapper<>();
        qw.eq("user_id", userId).eq("deleted", 0);
        if (isRead != null) qw.eq("is_read", isRead);
        qw.orderByDesc("create_time");

        long total = sysMessageMapper.selectCount(qw);

        QueryWrapper<SysMessage> pageQw = qw.clone()
                .last("LIMIT " + pageSize + " OFFSET " + ((pageNum - 1) * pageSize));
        List<SysMessage> records = sysMessageMapper.selectList(pageQw);

        List<MessageVO> vos = new ArrayList<>(records.size());
        for (SysMessage m : records) vos.add(toMessageVO(m));
        return new PageResult<>(total, pageNum, pageSize, vos);
    }

    @Override
    public long unreadCount(Long userId) {
        return sysMessageMapper.selectCount(
                new QueryWrapper<SysMessage>()
                        .eq("user_id", userId)
                        .eq("is_read", 0)
                        .eq("deleted", 0));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long id, Long userId) {
        SysMessage msg = sysMessageMapper.selectById(id);
        if (msg == null || msg.getDeleted() != null && msg.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "站内信不存在");
        }
        if (!msg.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权标记该站内信");
        }
        msg.setIsRead(1);
        msg.setReadTime(LocalDateTime.now());
        msg.setUpdateBy(userId);
        sysMessageMapper.updateById(msg);
    }

    // ========== Webhook ==========

    @Override
    public PageResult<WebhookConfigVO> listWebhooks(int pageNum, int pageSize) {
        QueryWrapper<WebhookConfig> qw = new QueryWrapper<>();
        qw.eq("deleted", 0).orderByDesc("create_time");
        long total = webhookConfigMapper.selectCount(qw);

        QueryWrapper<WebhookConfig> pageQw = qw.clone()
                .last("LIMIT " + pageSize + " OFFSET " + ((pageNum - 1) * pageSize));
        List<WebhookConfig> records = webhookConfigMapper.selectList(pageQw);

        List<WebhookConfigVO> vos = new ArrayList<>(records.size());
        for (WebhookConfig c : records) vos.add(toWebhookVO(c));
        return new PageResult<>(total, pageNum, pageSize, vos);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WebhookConfigVO createWebhook(WebhookCreateRequest request, Long operatorId) {
        log.info("[createWebhook] name={} url={}", request.getName(), request.getUrl());

        WebhookConfig c = new WebhookConfig();
        c.setName(request.getName());
        c.setUrl(request.getUrl());
        c.setEvents(request.getEvents());
        c.setSecret(request.getSecret());
        c.setStatus(request.getStatus() == null ? WEBHOOK_ENABLED : request.getStatus());
        c.setCreateBy(operatorId);
        c.setUpdateBy(operatorId);
        webhookConfigMapper.insert(c);

        return toWebhookVO(c);
    }

    @Override
    public WebhookTestResultVO testWebhook(Long id) {
        WebhookConfig c = webhookConfigMapper.selectById(id);
        if (c == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "Webhook 不存在");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (c.getSecret() != null) {
            headers.set("X-Webhook-Secret", c.getSecret());
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("event", "test");
        payload.put("webhookId", c.getId());
        payload.put("timestamp", System.currentTimeMillis());

        long start = System.currentTimeMillis();
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(c.getUrl(),
                    new HttpEntity<>(payload, headers), String.class);
            long cost = System.currentTimeMillis() - start;
            return WebhookTestResultVO.builder()
                    .webhookId(c.getId())
                    .url(c.getUrl())
                    .httpStatus(response.getStatusCode().value())
                    .responseBody(truncate(response.getBody(), 500))
                    .success(response.getStatusCode().is2xxSuccessful())
                    .costMs(cost)
                    .build();
        } catch (Exception ex) {
            long cost = System.currentTimeMillis() - start;
            log.warn("[testWebhook] failed url={} err={}", c.getUrl(), ex.getMessage());
            return WebhookTestResultVO.builder()
                    .webhookId(c.getId())
                    .url(c.getUrl())
                    .success(false)
                    .errorMessage(ex.getMessage())
                    .costMs(cost)
                    .build();
        }
    }

    // ========== AI 抽题 ==========

    @Override
    public ExtractedQuestionVO aiExtractQuestions(AiExtractRequest request) {
        log.info("[aiExtractQuestions] docId={} expectedCount={}",
                request.getDocId(), request.getExpectedCount());

        // W2 调用 AI Mock；W4 接入企业大模型（专业 Prompt）
        String prompt = String.format(
                "请从文档 #%d 中抽取 %d 道%s题目，以 JSON 数组返回，" +
                        "每个元素包含 type/stem/options/correctAnswer/knowledgePoint/difficulty 字段。",
                request.getDocId(),
                request.getExpectedCount() == null ? 5 : request.getExpectedCount(),
                request.getPreferredTypes() == null ? "" : "（偏好类型：" + request.getPreferredTypes() + "）"
        );

        String aiResp;
        try {
            aiResp = llmService.invoke(prompt);
        } catch (Exception ex) {
            log.error("[aiExtractQuestions] AI invoke failed", ex);
            throw new BusinessException(ErrorCode.AI_INVOKE_FAIL, "AI 抽题失败: " + ex.getMessage());
        }

        // W2 简化：从 AI 返回的 JSON 中解析（即使 Mock 也尽量结构化）
        // Mock 返回示例：{"questions":[{"type":"SINGLE","stem":"示例","options":[...]}]}
        // 解析失败时返回 1 个占位题
        List<ExtractedQuestionVO.Question> questions =
                new ArrayList<>(parseExtractedQuestions(aiResp));

        // 兜底：保证至少返回 1 题
        if (questions.isEmpty()) {
            questions.add(ExtractedQuestionVO.Question.builder()
                    .type("SINGLE")
                    .stem("AI 暂未返回有效题目，请稍后重试")
                    .correctAnswer("A")
                    .difficulty("MEDIUM")
                    .build());
        }

        return ExtractedQuestionVO.builder()
                .docId(request.getDocId())
                .questions(questions)
                .build();
    }

    /**
     * 简易 JSON 解析（仅在 W2 Mock 场景下使用；W4 可换 Jackson）。
     */
    @SuppressWarnings("unchecked")
    private List<ExtractedQuestionVO.Question> parseExtractedQuestions(String aiResp) {
        if (aiResp == null || aiResp.isBlank()) return Collections.emptyList();
        try {
            // 找 JSON 数组起始
            int start = aiResp.indexOf('[');
            int end = aiResp.lastIndexOf(']');
            if (start < 0 || end <= start) return Collections.emptyList();

            com.fasterxml.jackson.databind.ObjectMapper om =
                    new com.fasterxml.jackson.databind.ObjectMapper();
            return om.readValue(aiResp.substring(start, end + 1),
                    om.getTypeFactory().constructCollectionType(List.class,
                            ExtractedQuestionVO.Question.class));
        } catch (Exception ex) {
            log.warn("[parseExtractedQuestions] failed: {}", ex.getMessage());
            return Collections.emptyList();
        }
    }

    // ========== 私有 ==========

    private MessageVO toMessageVO(SysMessage m) {
        return MessageVO.builder()
                .id(m.getId())
                .userId(m.getUserId())
                .type(m.getType())
                .title(m.getTitle())
                .content(m.getContent())
                .isRead(m.getIsRead())
                .readTime(m.getReadTime())
                .createTime(m.getCreateTime())
                .build();
    }

    private WebhookConfigVO toWebhookVO(WebhookConfig c) {
        return WebhookConfigVO.builder()
                .id(c.getId())
                .name(c.getName())
                .url(c.getUrl())
                .events(c.getEvents())
                .hasSecret(c.getSecret() != null && !c.getSecret().isBlank())
                .status(c.getStatus())
                .createTime(c.getCreateTime())
                .updateTime(c.getUpdateTime())
                .build();
    }

    private String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }
}
