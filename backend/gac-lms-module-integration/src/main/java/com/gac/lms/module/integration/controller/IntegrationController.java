package com.gac.lms.module.integration.controller;

import com.gac.lms.common.constants.CommonConstants;
import com.gac.lms.common.exception.BusinessException;
import com.gac.lms.common.response.ErrorCode;
import com.gac.lms.common.response.PageResult;
import com.gac.lms.common.response.Result;
import com.gac.lms.module.integration.dto.AiExtractRequest;
import com.gac.lms.module.integration.dto.EmailSendRequest;
import com.gac.lms.module.integration.dto.MessagePushRequest;
import com.gac.lms.module.integration.dto.WebhookCreateRequest;
import com.gac.lms.module.integration.service.IntegrationService;
import com.gac.lms.module.integration.vo.EmailSendResultVO;
import com.gac.lms.module.integration.vo.ExtractedQuestionVO;
import com.gac.lms.module.integration.vo.MessageVO;
import com.gac.lms.module.integration.vo.WebhookConfigVO;
import com.gac.lms.module.integration.vo.WebhookTestResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 系统集成 Controller（3.3.11）。
 *
 * @author 方雨菲
 */
@Slf4j
@Tag(name = "系统集成", description = "3.3.11 邮件 / 站内信 / Webhook / AI 抽题")
@RestController
@RequestMapping("/api/v1/integration")
@RequiredArgsConstructor
public class IntegrationController {

    private final IntegrationService integrationService;

    @Operation(summary = "健康检查")
    @GetMapping("/health")
    public Result<String> health() {
        return Result.ok("integration-module-ok");
    }

    // ========== 邮件 ==========

    @Operation(summary = "发送邮件（通用或模板）")
    @PostMapping("/email/send")
    public Result<EmailSendResultVO> sendEmail(@Valid @RequestBody EmailSendRequest request) {
        return Result.ok(integrationService.sendEmail(request));
    }

    @Operation(summary = "模板邮件（EXAM_REMIND / GRADE_PUBLISH）")
    @PostMapping("/email/template")
    public Result<EmailSendResultVO> sendTemplate(@Valid @RequestBody EmailSendRequest request) {
        if (request.getTemplateCode() == null || request.getTemplateCode().isBlank()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "templateCode 不能为空");
        }
        return Result.ok(integrationService.sendEmail(request));
    }

    // ========== 站内信 ==========

    @Operation(summary = "推送一条站内信")
    @PostMapping("/message/push")
    public Result<MessageVO> pushMessage(
            @Valid @RequestBody MessagePushRequest request,
            @RequestHeader(value = CommonConstants.HEADER_USER_ID, required = false) Long operatorId) {
        // 系统推送 operatorId 可能为 0 或 null
        return Result.ok(integrationService.pushMessage(request));
    }

    @Operation(summary = "当前用户收件箱")
    @GetMapping("/message/list")
    public Result<PageResult<MessageVO>> listMessages(
            @RequestHeader(value = CommonConstants.HEADER_USER_ID, required = false) Long userId,
            @RequestParam(required = false) Integer isRead,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        requireUser(userId);
        return Result.ok(integrationService.listMessages(userId, isRead, pageNum, pageSize));
    }

    @Operation(summary = "未读数")
    @GetMapping("/message/unread-count")
    public Result<Map<String, Long>> unreadCount(
            @RequestHeader(value = CommonConstants.HEADER_USER_ID, required = false) Long userId) {
        requireUser(userId);
        Map<String, Long> data = new HashMap<>();
        data.put("unreadCount", integrationService.unreadCount(userId));
        return Result.ok(data);
    }

    @Operation(summary = "标记已读")
    @PostMapping("/message/{id}/read")
    public Result<Void> markAsRead(
            @PathVariable Long id,
            @RequestHeader(value = CommonConstants.HEADER_USER_ID, required = false) Long userId) {
        requireUser(userId);
        integrationService.markAsRead(id, userId);
        return Result.ok();
    }

    // ========== Webhook ==========

    @Operation(summary = "Webhook 配置列表")
    @GetMapping("/webhooks")
    public Result<PageResult<WebhookConfigVO>> listWebhooks(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.ok(integrationService.listWebhooks(pageNum, pageSize));
    }

    @Operation(summary = "新增 Webhook 配置")
    @PostMapping("/webhooks")
    public Result<WebhookConfigVO> createWebhook(
            @Valid @RequestBody WebhookCreateRequest request,
            @RequestHeader(value = CommonConstants.HEADER_USER_ID, required = false) Long operatorId) {
        requireUser(operatorId);
        return Result.ok(integrationService.createWebhook(request, operatorId));
    }

    @Operation(summary = "测试推送")
    @PostMapping("/webhooks/{id}/test")
    public Result<WebhookTestResultVO> testWebhook(@PathVariable Long id) {
        return Result.ok(integrationService.testWebhook(id));
    }

    // ========== AI 抽题 ==========

    @Operation(summary = "AI 文档 → 题目抽取（W2 Mock，W4 接真实模型）")
    @PostMapping("/ai/extract")
    public Result<ExtractedQuestionVO> aiExtract(@Valid @RequestBody AiExtractRequest request) {
        return Result.ok(integrationService.aiExtractQuestions(request));
    }

    private void requireUser(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "未识别用户身份");
        }
    }
}
