package com.gac.lms.module.integration.controller;

import com.gac.lms.common.response.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统集成 Controller（W1 骨架）。
 *
 * <p>W4 完整实现：</p>
 * <ul>
 *   <li>{@code POST /api/v1/integration/email/send} —— 发送邮件</li>
 *   <li>{@code POST /api/v1/integration/email/template}  —— 模板邮件（考试提醒）</li>
 *   <li>{@code POST /api/v1/integration/webhook/test}    —— Webhook 测试推送</li>
 *   <li>{@code GET  /api/v1/integration/webhooks}        —— Webhook 配置列表</li>
 *   <li>{@code POST /api/v1/integration/message/push}    —— 站内信推送</li>
 * </ul>
 *
 * @author 方雨菲
 */
@Tag(name = "系统集成", description = "3.3.11 邮件 / Webhook / 站内信触发")
@RestController
@RequestMapping("/api/v1/integration")
public class IntegrationController {

    @Operation(summary = "健康检查（W1 验收用）")
    @GetMapping("/health")
    public Result<String> health() {
        return Result.ok("integration-module-ok");
    }
}
