package com.gac.lms.module.selfTest.controller;

import com.gac.lms.common.constants.CommonConstants;
import com.gac.lms.common.exception.BusinessException;
import com.gac.lms.common.response.ErrorCode;
import com.gac.lms.common.response.PageResult;
import com.gac.lms.common.response.Result;
import com.gac.lms.module.selfTest.service.SelfTestService;
import com.gac.lms.module.selfTest.vo.ExamItemVO;
import com.gac.lms.module.selfTest.vo.WrongQuestionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 个人测评 Controller（3.3.9）。
 *
 * @author 方雨菲
 */
@Slf4j
@Tag(name = "个人测评", description = "3.3.9 我的考试 / 错题本 / AI 解析")
@RestController
@RequestMapping("/api/v1/self-test")
@RequiredArgsConstructor
public class SelfTestController {

    private final SelfTestService selfTestService;

    @Operation(summary = "健康检查")
    @GetMapping("/health")
    public Result<String> health() {
        return Result.ok("self-test-module-ok");
    }

    @Operation(summary = "待考列表")
    @GetMapping("/exams/pending")
    public Result<PageResult<ExamItemVO>> pendingExams(
            @RequestHeader(value = CommonConstants.HEADER_USER_ID, required = false) Long userId,
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "20") long pageSize) {
        requireUser(userId);
        return Result.ok(selfTestService.listPendingExams(userId, pageNum, pageSize));
    }

    @Operation(summary = "已考列表")
    @GetMapping("/exams/finished")
    public Result<PageResult<ExamItemVO>> finishedExams(
            @RequestHeader(value = CommonConstants.HEADER_USER_ID, required = false) Long userId,
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "20") long pageSize) {
        requireUser(userId);
        return Result.ok(selfTestService.listFinishedExams(userId, pageNum, pageSize));
    }

    @Operation(summary = "错题本")
    @GetMapping("/wrong-questions")
    public Result<PageResult<WrongQuestionVO>> wrongQuestions(
            @RequestHeader(value = CommonConstants.HEADER_USER_ID, required = false) Long userId,
            @Parameter(description = "是否已掌握（0/1，null=全部）")
            @RequestParam(required = false) Integer isMastered,
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "20") long pageSize) {
        requireUser(userId);
        return Result.ok(selfTestService.listWrongQuestions(userId, isMastered, pageNum, pageSize));
    }

    @Operation(summary = "错题详情")
    @GetMapping("/wrong-questions/{id}")
    public Result<WrongQuestionVO> wrongQuestionDetail(
            @PathVariable Long id,
            @RequestHeader(value = CommonConstants.HEADER_USER_ID, required = false) Long userId) {
        requireUser(userId);
        return Result.ok(selfTestService.getWrongQuestion(id, userId));
    }

    @Operation(summary = "触发 AI 解析（W2 走 Mock，W4 接真实模型）")
    @PostMapping("/wrong-questions/{id}/ai")
    public Result<WrongQuestionVO> triggerAi(
            @PathVariable Long id,
            @RequestHeader(value = CommonConstants.HEADER_USER_ID, required = false) Long userId) {
        requireUser(userId);
        return Result.ok(selfTestService.triggerAiExplanation(id, userId));
    }

    @Operation(summary = "标记错题为已掌握")
    @PostMapping("/wrong-questions/{id}/master")
    public Result<Void> markMastered(
            @PathVariable Long id,
            @RequestHeader(value = CommonConstants.HEADER_USER_ID, required = false) Long userId) {
        requireUser(userId);
        selfTestService.markAsMastered(id, userId);
        return Result.ok();
    }

    private void requireUser(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "未识别用户身份");
        }
    }
}
