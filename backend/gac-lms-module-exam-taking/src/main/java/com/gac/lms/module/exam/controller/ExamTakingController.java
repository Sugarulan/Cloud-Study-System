package com.gac.lms.module.exam.controller;

import com.gac.lms.common.response.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 在线作答 Controller（W1 骨架）。
 *
 * <p>W2 完成：试卷渲染、答题暂存（Redis）、断点续答、计时、自动交卷。</p>
 *
 * @author 方雨菲
 */
@Tag(name = "在线作答", description = "3.3.8 学员在线答题")
@RestController
@RequestMapping("/api/v1/exam-taking")
public class ExamTakingController {

    @Operation(summary = "健康检查（W1 验收用）")
    @GetMapping("/health")
    public Result<String> health() {
        return Result.ok("exam-taking-module-ok");
    }

    @Operation(summary = "获取试卷（占位）")
    @GetMapping("/{examId}/paper")
    public Result<String> getPaper(@PathVariable Long examId) {
        return Result.ok("TODO: W2 调用试卷模块获取题目列表, examId=" + examId);
    }

    @Operation(summary = "答题暂存（占位）")
    @PostMapping("/{examId}/save")
    public Result<String> saveAnswer(@PathVariable Long examId) {
        return Result.ok("TODO: W2 实现 Redis 暂存, examId=" + examId);
    }
}
