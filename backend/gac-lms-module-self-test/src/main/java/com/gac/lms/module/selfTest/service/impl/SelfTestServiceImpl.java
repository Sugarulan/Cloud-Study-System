package com.gac.lms.module.selfTest.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gac.lms.ai.service.LLMService;
import com.gac.lms.common.exception.BusinessException;
import com.gac.lms.common.response.ErrorCode;
import com.gac.lms.common.response.PageResult;
import com.gac.lms.module.selfTest.entity.GradeRecordView;
import com.gac.lms.module.selfTest.entity.WrongQuestion;
import com.gac.lms.module.selfTest.mapper.GradeRecordViewMapper;
import com.gac.lms.module.selfTest.mapper.WrongQuestionMapper;
import com.gac.lms.module.selfTest.service.SelfTestService;
import com.gac.lms.module.selfTest.vo.ExamItemVO;
import com.gac.lms.module.selfTest.vo.WrongQuestionVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 个人测评 Service 实现。
 *
 * <h3>W2 简化策略</h3>
 * <ul>
 *   <li>待考列表：当前实现返回空（需要接入王茗瑾的考试参考范围接口，W3 联调）</li>
 *   <li>已考列表：从 {@code grade_record} 直接查询（status = 4 已发布）</li>
 *   <li>错题本：从 {@code wrong_question} 表查询</li>
 *   <li>AI 解析：调用 {@code LLMService.invoke}，结果回写</li>
 * </ul>
 *
 * @author 方雨菲
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SelfTestServiceImpl implements SelfTestService {

    /** grade_record 状态：已发布 */
    private static final int STATUS_PUBLISHED = 4;

    private final WrongQuestionMapper wrongQuestionMapper;
    private final GradeRecordViewMapper gradeRecordViewMapper;
    private final LLMService llmService;

    @Override
    public PageResult<ExamItemVO> listPendingExams(Long userId, long pageNum, long pageSize) {
        // W3 联调：调用王茗瑾的"考试参考范围"接口，减去已考集合
        log.debug("[listPendingExams] userId={} (W3 联调后填充)", userId);
        return new PageResult<>(0L, pageNum, pageSize, Collections.emptyList());
    }

    @Override
    public PageResult<ExamItemVO> listFinishedExams(Long userId, long pageNum, long pageSize) {
        log.debug("[listFinishedExams] userId={} page={}/{}", userId, pageNum, pageSize);

        // 仅查已发布（status=4）
        QueryWrapper<GradeRecordView> qw = new QueryWrapper<>();
        qw.eq("user_id", userId)
                .eq("status", STATUS_PUBLISHED)
                .eq("deleted", 0)
                .orderByDesc("submitted_at");

        long total = gradeRecordViewMapper.selectCount(qw);

        QueryWrapper<GradeRecordView> pageQw = qw.clone()
                .last("LIMIT " + pageSize + " OFFSET " + ((pageNum - 1) * pageSize));
        List<GradeRecordView> records = gradeRecordViewMapper.selectList(pageQw);

        // 统计每个 gradeId 的错题数（批量查询减少 N+1）
        Map<Long, Long> wrongCountMap = countWrongByGradeIds(
                records.stream().map(GradeRecordView::getId).collect(Collectors.toList()));

        List<ExamItemVO> items = new ArrayList<>(records.size());
        for (GradeRecordView r : records) {
            items.add(ExamItemVO.builder()
                    .examId(r.getExamId())
                    .paperId(r.getPaperId())
                    .statusLabel("已发布")
                    .gradeId(r.getId())
                    .userScore(r.getTotalScore())
                    .isPassed(r.getIsPassed())
                    .wrongCount(wrongCountMap.getOrDefault(r.getId(), 0L).intValue())
                    .submittedAt(r.getSubmittedAt())
                    .build());
        }
        return new PageResult<>(total, pageNum, pageSize, items);
    }

    @Override
    public PageResult<WrongQuestionVO> listWrongQuestions(Long userId, Integer isMastered,
                                                          long pageNum, long pageSize) {
        log.debug("[listWrongQuestions] userId={} isMastered={} page={}/{}",
                userId, isMastered, pageNum, pageSize);

        QueryWrapper<WrongQuestion> qw = new QueryWrapper<>();
        qw.eq("user_id", userId).eq("deleted", 0);
        if (isMastered != null) qw.eq("is_mastered", isMastered);
        qw.orderByDesc("create_time");

        long total = wrongQuestionMapper.selectCount(qw);

        QueryWrapper<WrongQuestion> pageQw = qw.clone()
                .last("LIMIT " + pageSize + " OFFSET " + ((pageNum - 1) * pageSize));
        List<WrongQuestion> records = wrongQuestionMapper.selectList(pageQw);

        List<WrongQuestionVO> items = records.stream().map(this::toVO).collect(Collectors.toList());
        return new PageResult<>(total, pageNum, pageSize, items);
    }

    @Override
    public WrongQuestionVO getWrongQuestion(Long id, Long userId) {
        WrongQuestion wq = mustGet(id, userId);
        return toVO(wq);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WrongQuestionVO triggerAiExplanation(Long id, Long userId) {
        WrongQuestion wq = mustGet(id, userId);

        // 已解析且 24h 内不重复触发
        if (wq.getAiExplanation() != null && wq.getAiExplainedAt() != null
                && wq.getAiExplainedAt().isAfter(LocalDateTime.now().minusHours(24))) {
            log.debug("[triggerAiExplanation] cache hit, id={}", id);
            return toVO(wq);
        }

        String prompt = buildExplanationPrompt(wq);
        String aiText;
        try {
            aiText = llmService.invokeSafely(prompt);
        } catch (Exception ex) {
            log.error("[triggerAiExplanation] AI invoke failed", ex);
            throw new BusinessException(ErrorCode.AI_INVOKE_FAIL,
                    "AI 解析失败: " + ex.getMessage());
        }

        wq.setAiExplanation(aiText != null && !aiText.isBlank() ? aiText
                : "（AI 暂未返回结果，请稍后再试）");
        wq.setAiExplainedAt(LocalDateTime.now());
        wq.setUpdateBy(userId);
        wrongQuestionMapper.updateById(wq);
        log.info("[triggerAiExplanation] id={} userId={} aiText.length={}", id, userId,
                wq.getAiExplanation().length());

        return toVO(wq);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsMastered(Long id, Long userId) {
        WrongQuestion wq = mustGet(id, userId);
        wq.setIsMastered(1);
        wq.setUpdateBy(userId);
        wrongQuestionMapper.updateById(wq);
    }

    // ===== 私有 =====

    private WrongQuestion mustGet(Long id, Long userId) {
        WrongQuestion wq = wrongQuestionMapper.selectById(id);
        if (wq == null || wq.getDeleted() != null && wq.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "错题不存在");
        }
        if (!wq.getUserId().equals(userId)) {
            // 防止越权查看别人的错题
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权访问该错题");
        }
        return wq;
    }

    private Map<Long, Long> countWrongByGradeIds(List<Long> gradeIds) {
        if (gradeIds.isEmpty()) return Collections.emptyMap();
        // W2 简化：用 wrong_question 表的 grade_id 字段统计
        QueryWrapper<WrongQuestion> qw = new QueryWrapper<>();
        qw.in("grade_id", gradeIds).eq("deleted", 0);
        List<WrongQuestion> list = wrongQuestionMapper.selectList(qw);
        Map<Long, Long> map = new HashMap<>();
        for (WrongQuestion w : list) {
            // 用 compute 而非 merge 避免 BiFunction 类型推断警告
            map.compute(w.getGradeId(), (k, v) -> v == null ? 1L : v + 1L);
        }
        return map;
    }

    private String buildExplanationPrompt(WrongQuestion wq) {
        // W4 切换为模板化 Prompt；W2 简化拼接
        return String.format(
                "请为以下错题提供详细解析：\n\n题目ID：%d\n学员答案：%s\n正确答案：%s\n\n" +
                        "请说明：1) 错误原因 2) 考查的知识点 3) 类似题建议",
                wq.getQuestionId(),
                wq.getUserAnswer() == null ? "（空）" : wq.getUserAnswer(),
                wq.getCorrectAnswer() == null ? "（未知）" : wq.getCorrectAnswer());
    }

    private WrongQuestionVO toVO(WrongQuestion w) {
        return WrongQuestionVO.builder()
                .id(w.getId())
                .examId(w.getExamId() == null ? null : w.getExamId())
                .paperId(null)
                .questionId(w.getQuestionId())
                // .stem / .optionsJson / .questionType W3 接入题目服务后填充
                .userAnswer(w.getUserAnswer())
                .correctAnswer(w.getCorrectAnswer())
                .isMastered(w.getIsMastered())
                .aiExplanation(w.getAiExplanation())
                .aiExplainedAt(w.getAiExplainedAt())
                .build();
    }
}
