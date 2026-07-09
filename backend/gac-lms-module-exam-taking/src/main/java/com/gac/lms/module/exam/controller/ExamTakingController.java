package com.gac.lms.module.exam.controller;

import com.gac.lms.common.constants.CommonConstants;
import com.gac.lms.common.exception.BusinessException;
import com.gac.lms.common.response.ErrorCode;
import com.gac.lms.common.response.Result;
import com.gac.lms.module.exam.dto.SaveAllRequest;
import com.gac.lms.module.exam.dto.SaveAnswerRequest;
import com.gac.lms.module.exam.service.ExamTakingService;
import com.gac.lms.module.exam.vo.PaperRenderVO;
import com.gac.lms.module.exam.vo.RemainingTimeVO;
import com.gac.lms.module.exam.vo.SubmitResultVO;
import com.gac.lms.module.exam.vo.TakingSnapshotVO;
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
import org.springframework.web.bind.annotation.RestController;

/**
 * 在线作答 Controller（3.3.8）。
 *
 * <p>W2 完整实现：7 个核心接口。
 * W3 联调：将替换 {@code PaperQueryService} 为远程调用，与王茗瑾模块对齐。</p>
 *
 * @author 方雨菲
 */
@Slf4j
@Tag(name = "在线作答", description = "3.3.8 学员在线答题：渲染/暂存/续答/计时/交卷")
@RestController
@RequestMapping("/api/v1/exam-taking")
@RequiredArgsConstructor
public class ExamTakingController {

    private final ExamTakingService examTakingService;

    @Operation(summary = "健康检查")
    @GetMapping("/health")
    public Result<String> health() {
        return Result.ok("exam-taking-module-ok");
    }

    @Operation(summary = "获取试卷（首次进入初始化 Redis 快照）")
    @GetMapping("/{examId}/paper")
    public Result<PaperRenderVO> renderPaper(
            @Parameter(description = "考试 ID") @PathVariable Long examId,
            @Parameter(description = "学员 ID（由网关注入）")
            @RequestHeader(value = CommonConstants.HEADER_USER_ID, required = false) Long userId) {
        requireUser(userId);
        return Result.ok(examTakingService.renderPaper(examId, userId));
    }

    @Operation(summary = "单题暂存（含乐观锁版本检查）")
    @PostMapping("/{examId}/save")
    public Result<Integer> saveAnswer(
            @PathVariable Long examId,
            @RequestHeader(value = CommonConstants.HEADER_USER_ID, required = false) Long userId,
            @Valid @RequestBody SaveAnswerRequest request) {
        requireUser(userId);
        return Result.ok(examTakingService.saveAnswer(examId, userId, request));
    }

    @Operation(summary = "批量暂存")
    @PostMapping("/{examId}/save-all")
    public Result<Integer> saveAll(
            @PathVariable Long examId,
            @RequestHeader(value = CommonConstants.HEADER_USER_ID, required = false) Long userId,
            @Valid @RequestBody SaveAllRequest request) {
        requireUser(userId);
        return Result.ok(examTakingService.saveAll(examId, userId, request));
    }

    @Operation(summary = "获取答卷快照（断点续答）")
    @GetMapping("/{examId}/snapshot")
    public Result<TakingSnapshotVO> getSnapshot(
            @PathVariable Long examId,
            @RequestHeader(value = CommonConstants.HEADER_USER_ID, required = false) Long userId) {
        requireUser(userId);
        return Result.ok(examTakingService.getSnapshot(examId, userId));
    }

    @Operation(summary = "获取考试剩余时间")
    @GetMapping("/{examId}/remaining")
    public Result<RemainingTimeVO> getRemaining(
            @PathVariable Long examId,
            @RequestHeader(value = CommonConstants.HEADER_USER_ID, required = false) Long userId) {
        requireUser(userId);
        return Result.ok(examTakingService.getRemaining(examId, userId));
    }

    @Operation(summary = "交卷（持久化 + 清空 Redis 快照）")
    @PostMapping("/{examId}/submit")
    public Result<SubmitResultVO> submit(
            @PathVariable Long examId,
            @RequestHeader(value = CommonConstants.HEADER_USER_ID, required = false) Long userId) {
        requireUser(userId);
        return Result.ok(examTakingService.submit(examId, userId));
    }

    private void requireUser(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "未识别用户身份");
        }
        log.debug("[{}] userId={}", Thread.currentThread().getStackTrace()[2].getMethodName(), userId);
    }
}
