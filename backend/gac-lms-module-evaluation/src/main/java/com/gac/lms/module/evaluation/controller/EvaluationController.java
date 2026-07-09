package com.gac.lms.module.evaluation.controller;

import com.gac.lms.common.constants.CommonConstants;
import com.gac.lms.common.exception.BusinessException;
import com.gac.lms.common.response.ErrorCode;
import com.gac.lms.common.response.PageResult;
import com.gac.lms.common.response.Result;
import com.gac.lms.module.evaluation.dto.AiEvaluateRequest;
import com.gac.lms.module.evaluation.dto.AutoEvaluateRequest;
import com.gac.lms.module.evaluation.dto.ManualEvaluateRequest;
import com.gac.lms.module.evaluation.service.EvaluationService;
import com.gac.lms.module.evaluation.vo.EvaluationActionVO;
import com.gac.lms.module.evaluation.vo.EvaluationResultVO;
import com.gac.lms.module.evaluation.vo.PendingItemVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

/**
 * 评卷 Controller（3.3.6）。
 *
 * <p>W2 完整实现：客观题自动判分 + 主观题 AI 评阅 + 人工评卷 + 复核 + 发布。</p>
 *
 * @author 方雨菲
 */
@Slf4j
@Tag(name = "评卷模块", description = "3.3.6 自动 / AI / 人工 / 复核 / 发布")
@RestController
@RequestMapping("/api/v1/evaluation")
@RequiredArgsConstructor
public class EvaluationController {

    private final EvaluationService evaluationService;

    @Operation(summary = "健康检查")
    @GetMapping("/health")
    public Result<String> health() {
        return Result.ok("evaluation-module-ok");
    }

    @Operation(summary = "客观题自动评阅")
    @PostMapping("/auto")
    public Result<EvaluationResultVO> autoEvaluate(@Valid @RequestBody AutoEvaluateRequest request) {
        return Result.ok(evaluationService.autoEvaluate(request));
    }

    @Operation(summary = "主观题 AI 评阅（W2 走 Mock，W4 接入企业大模型）")
    @PostMapping("/ai")
    public Result<EvaluationResultVO> aiEvaluate(@Valid @RequestBody AiEvaluateRequest request) {
        return Result.ok(evaluationService.aiEvaluate(request));
    }

    @Operation(summary = "人工评阅")
    @PostMapping("/manual")
    public Result<EvaluationResultVO> manualEvaluate(
            @Valid @RequestBody ManualEvaluateRequest request,
            @RequestHeader(value = CommonConstants.HEADER_USER_ID, required = false) Long userId) {
        requireUser(userId);
        return Result.ok(evaluationService.manualEvaluate(request, userId));
    }

    @Operation(summary = "评卷复核")
    @PostMapping("/{id}/review")
    public Result<EvaluationActionVO> review(
            @PathVariable Long id,
            @RequestHeader(value = CommonConstants.HEADER_USER_ID, required = false) Long userId) {
        requireUser(userId);
        return Result.ok(evaluationService.review(id, userId));
    }

    @Operation(summary = "成绩发布")
    @PostMapping("/{id}/publish")
    public Result<EvaluationActionVO> publish(
            @PathVariable Long id,
            @RequestHeader(value = CommonConstants.HEADER_USER_ID, required = false) Long userId) {
        requireUser(userId);
        return Result.ok(evaluationService.publish(id, userId));
    }

    @Operation(summary = "待评卷列表（人工评卷工作台）")
    @GetMapping("/pending")
    public Result<PageResult<PendingItemVO>> listPending(
            @Parameter(description = "考试 ID（可选）") @RequestParam(required = false) Long examId,
            @Parameter(description = "状态（可选：0=待评 1=部分 2=已评 3=已复核 4=已发布）")
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.ok(evaluationService.listPending(examId, status, pageNum, pageSize));
    }

    @Operation(summary = "获取成绩详情")
    @GetMapping("/{id}")
    public Result<EvaluationResultVO> getDetail(@PathVariable Long id) {
        return Result.ok(evaluationService.getDetail(id));
    }

    private void requireUser(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "未识别用户身份");
        }
    }
}
