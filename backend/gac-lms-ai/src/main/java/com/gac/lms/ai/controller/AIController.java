package com.gac.lms.ai.controller;

import com.gac.lms.ai.provider.LLMRequest;
import com.gac.lms.ai.service.LLMService;
import com.gac.lms.common.response.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 模块对外接口（W1 骨架）。
 *
 * <p>完整实现见 W4：AI 评卷、AI 错题解析、AI 文档→题目抽取。</p>
 *
 * @author 方雨菲
 */
@Tag(name = "AI 大模型", description = "AI 评卷 / 错题解析 / 文档抽题")
@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AIController {

    private final LLMService llmService;

    @Operation(summary = "通用 LLM 调用（W1 调试用）")
    @PostMapping("/invoke")
    public Result<String> invoke(@RequestBody LLMRequest request) {
        if (request.getPrompt() == null || request.getPrompt().isBlank()) {
            return Result.fail(10001, "prompt 不能为空");
        }
        String text = llmService.invoke(request.getPrompt());
        return Result.ok(text);
    }
}
