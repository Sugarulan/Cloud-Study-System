package com.gac.lms.module.evaluation.controller;

import com.gac.lms.common.response.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 评卷模块 Controller（W1 骨架）。
 *
 * <p>W2 完成：客观题自动评阅、主观题 AI 评阅、人工评卷、复核、成绩发布。</p>
 *
 * @author 方雨菲
 */
@Tag(name = "评卷模块", description = "3.3.6 评卷：自动 / AI / 人工 / 复核 / 发布")
@RestController
@RequestMapping("/api/v1/evaluation")
public class EvaluationController {

    @Operation(summary = "健康检查（W1 验收用）")
    @GetMapping("/health")
    public Result<String> health() {
        return Result.ok("evaluation-module-ok");
    }

    @Operation(summary = "自动评阅（占位）")
    @PostMapping("/auto")
    public Result<String> autoEvaluate() {
        return Result.ok("TODO: W2 实现客观题自动判分");
    }

    @Operation(summary = "AI 评阅（占位）")
    @PostMapping("/ai")
    public Result<String> aiEvaluate() {
        return Result.ok("TODO: W4 实现主观题 AI 评阅");
    }

    @Operation(summary = "人工评阅（占位）")
    @PostMapping("/manual")
    public Result<String> manualEvaluate() {
        return Result.ok("TODO: W2 实现人工评卷");
    }
}
