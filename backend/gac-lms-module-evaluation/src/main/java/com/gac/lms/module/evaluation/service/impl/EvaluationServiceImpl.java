package com.gac.lms.module.evaluation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gac.lms.ai.service.LLMService;
import com.gac.lms.common.constants.CommonConstants;
import com.gac.lms.common.exception.BusinessException;
import com.gac.lms.common.response.ErrorCode;
import com.gac.lms.common.response.PageResult;
import com.gac.lms.module.evaluation.dto.AiEvaluateRequest;
import com.gac.lms.module.evaluation.dto.AutoEvaluateRequest;
import com.gac.lms.module.evaluation.dto.ManualEvaluateRequest;
import com.gac.lms.module.evaluation.entity.GradeDetail;
import com.gac.lms.module.evaluation.entity.GradeRecord;
import com.gac.lms.module.evaluation.mapper.GradeDetailMapper;
import com.gac.lms.module.evaluation.mapper.GradeRecordMapper;
import com.gac.lms.module.evaluation.service.EvaluationService;
import com.gac.lms.module.evaluation.vo.EvaluationActionVO;
import com.gac.lms.module.evaluation.vo.EvaluationResultVO;
import com.gac.lms.module.evaluation.vo.PendingItemVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 评卷 Service 实现。
 *
 * <p>核心算法：</p>
 * <ul>
 *   <li>SINGLE（单选）：答案完全相等 → 满分</li>
 *   <li>MULTI（多选）：集合相等 → 满分；否则 0 分</li>
 *   <li>JUDGE（判断）：true/false 相等 → 满分</li>
 *   <li>FILL（填空）：忽略大小写和空格比较</li>
 *   <li>ESSAY（主观题）：W2 调用 AI Mock 评 0 分待人工；W4 真实 AI 评分</li>
 * </ul>
 *
 * @author 方雨菲
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EvaluationServiceImpl implements EvaluationService {

    /** 评卷状态码 */
    public static final int STATUS_PENDING = 0;
    public static final int STATUS_PARTIAL = 1;
    public static final int STATUS_GRADED = 2;
    public static final int STATUS_REVIEWED = 3;
    public static final int STATUS_PUBLISHED = 4;

    /** 评卷方式 */
    public static final int EVAL_AUTO = 0;
    public static final int EVAL_AI = 1;
    public static final int EVAL_MANUAL = 2;

    /** 默认通过分 */
    public static final BigDecimal DEFAULT_PASS_SCORE = new BigDecimal("60");

    private final GradeRecordMapper gradeRecordMapper;
    private final GradeDetailMapper gradeDetailMapper;
    private final LLMService llmService;

    // ========== 自动评阅 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EvaluationResultVO autoEvaluate(AutoEvaluateRequest request) {
        log.info("[autoEvaluate] examId={} userId={} count={}",
                request.getExamId(), request.getUserId(),
                request.getAnswers() == null ? 0 : request.getAnswers().size());

        GradeRecord record = upsertGradeRecord(request.getExamId(), request.getUserId());

        BigDecimal totalScore = BigDecimal.ZERO;
        BigDecimal objectiveScore = BigDecimal.ZERO;
        int pendingManual = 0;
        List<EvaluationResultVO.QuestionResult> details = new ArrayList<>();

        for (AutoEvaluateRequest.QuestionAnswer qa : request.getAnswers()) {
            EvaluationResultVO.QuestionResult qr = evaluateOneObjective(qa);
            details.add(qr);

            if (qr.getScore() != null) {
                BigDecimal s = qr.getScore();
                totalScore = totalScore.add(s);
                if (isObjective(qa.getType())) {
                    objectiveScore = objectiveScore.add(s);
                }
            }
            if (pendingEssay(qa.getType())) {
                pendingManual++;
            }

            upsertGradeDetail(record.getId(), qa, qr);
        }

        // 状态判断：若还有主观题待评 → 1=部分；否则 → 2=已评
        int newStatus = pendingManual > 0 ? STATUS_PARTIAL : STATUS_GRADED;
        record.setObjectiveScore(objectiveScore);
        record.setTotalScore(totalScore);
        record.setStatus(newStatus);
        record.setUpdateBy(request.getUserId());
        gradeRecordMapper.updateById(record);

        BigDecimal passScore = record.getPassScore() != null ? record.getPassScore() : DEFAULT_PASS_SCORE;
        Integer isPassed = totalScore.compareTo(passScore) >= 0 ? 1 : 0;
        // 只有全部评完才更新 isPassed（避免部分评分时误导）
        if (newStatus == STATUS_GRADED) {
            record.setIsPassed(isPassed);
            gradeRecordMapper.updateById(record);
        }

        return EvaluationResultVO.builder()
                .gradeId(record.getId())
                .examId(request.getExamId())
                .userId(request.getUserId())
                .totalScore(totalScore)
                .objectiveScore(objectiveScore)
                .subjectiveScore(null)
                .pendingManualCount(pendingManual)
                .isPassed(newStatus == STATUS_GRADED ? isPassed : null)
                .status(newStatus)
                .details(details)
                .build();
    }

    /**
     * 单题客观题判分。
     */
    private EvaluationResultVO.QuestionResult evaluateOneObjective(AutoEvaluateRequest.QuestionAnswer qa) {
        EvaluationResultVO.QuestionResult qr = EvaluationResultVO.QuestionResult.builder()
                .questionId(qa.getQuestionId())
                .evaluatorType(EVAL_AUTO)
                .build();

        String type = qa.getType() == null ? "" : qa.getType().toUpperCase();
        BigDecimal full = BigDecimal.valueOf(qa.getFullScore() == null ? 0 : qa.getFullScore());
        String user = qa.getUserAnswer() == null ? "" : qa.getUserAnswer().trim();
        String correct = qa.getCorrectAnswer() == null ? "" : qa.getCorrectAnswer().trim();

        boolean correct2;
        switch (type) {
            case "SINGLE":
                correct2 = user.equalsIgnoreCase(correct);
                break;
            case "MULTI":
                correct2 = multiChoiceEqual(user, correct);
                break;
            case "JUDGE":
                correct2 = normalizeBoolean(user).equals(normalizeBoolean(correct));
                break;
            case "FILL":
                correct2 = fillEqual(user, correct);
                break;
            case "ESSAY":
                // 客观题接口不评主观题
                qr.setScore(null);
                qr.setIsCorrect(null);
                qr.setComment("主观题请走 AI 评阅或人工评阅");
                return qr;
            default:
                correct2 = false;
        }

        qr.setIsCorrect(correct2 ? 1 : 0);
        qr.setScore(correct2 ? full : BigDecimal.ZERO);
        return qr;
    }

    private static boolean multiChoiceEqual(String user, String correct) {
        Set<String> u = splitChoices(user);
        Set<String> c = splitChoices(correct);
        return u.equals(c);
    }

    private static Set<String> splitChoices(String s) {
        if (s == null || s.isEmpty()) return new HashSet<>();
        // 支持 "A,B,C" / "A B C" / "[\"A\",\"B\"]" 三种格式
        String t = s.replace("[", "").replace("]", "").replace("\"", "").trim();
        Set<String> set = new HashSet<>();
        for (String p : t.split("[,\\s]+")) {
            if (!p.isEmpty()) set.add(p.toUpperCase());
        }
        return set;
    }

    private static String normalizeBoolean(String s) {
        if (s == null) return "";
        String t = s.trim().toUpperCase();
        if (t.equals("TRUE") || t.equals("T") || t.equals("正确") || t.equals("对")) return "T";
        if (t.equals("FALSE") || t.equals("F") || t.equals("错误") || t.equals("错")) return "F";
        return t;
    }

    private static boolean fillEqual(String user, String correct) {
        // 填空题支持 "答案1|答案2" 表示任一可接受
        String[] accepts = correct.split("\\|");
        String u = user.replaceAll("\\s+", "").toLowerCase();
        for (String a : accepts) {
            if (u.equals(a.replaceAll("\\s+", "").toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private boolean isObjective(String type) {
        return Arrays.asList("SINGLE", "MULTI", "JUDGE", "FILL").contains(type == null ? "" : type.toUpperCase());
    }

    private boolean pendingEssay(String type) {
        return "ESSAY".equalsIgnoreCase(type);
    }

    // ========== AI 评阅 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EvaluationResultVO aiEvaluate(AiEvaluateRequest request) {
        log.info("[aiEvaluate] examId={} userId={} essays={}",
                request.getExamId(), request.getUserId(),
                request.getEssays() == null ? 0 : request.getEssays().size());

        GradeRecord record = upsertGradeRecord(request.getExamId(), request.getUserId());

        BigDecimal subjectiveScore = BigDecimal.ZERO;
        List<EvaluationResultVO.QuestionResult> details = new ArrayList<>();

        for (AiEvaluateRequest.EssayAnswer ea : request.getEssays()) {
            String prompt = buildAiPrompt(ea);
            String aiResp;
            try {
                aiResp = llmService.invoke(prompt);
            } catch (Exception ex) {
                log.error("[aiEvaluate] AI invoke failed: {}", ex.getMessage());
                throw new BusinessException(ErrorCode.EVALUATION_AI_FAIL, "AI 评卷失败: " + ex.getMessage());
            }
            AiScore parsed = parseAiResponse(aiResp, ea.getFullScore());

            EvaluationResultVO.QuestionResult qr = EvaluationResultVO.QuestionResult.builder()
                    .questionId(ea.getQuestionId())
                    .isCorrect(parsed.score.compareTo(BigDecimal.ZERO) > 0 ? 1 : 0)
                    .score(parsed.score)
                    .evaluatorType(EVAL_AI)
                    .comment(parsed.comment)
                    .build();
            details.add(qr);
            subjectiveScore = subjectiveScore.add(parsed.score);

            // 落库 grade_detail
            GradeDetail detail = new GradeDetail();
            detail.setGradeId(record.getId());
            detail.setQuestionId(ea.getQuestionId());
            detail.setUserAnswer(ea.getUserAnswer());
            detail.setCorrectAnswer(ea.getReferenceAnswer());
            detail.setIsCorrect(qr.getIsCorrect());
            detail.setScore(parsed.score);
            detail.setFullScore(BigDecimal.valueOf(ea.getFullScore()));
            detail.setEvaluatorType(EVAL_AI);
            detail.setComment(parsed.comment);
            detail.setCreateBy(request.getUserId());
            detail.setUpdateBy(request.getUserId());
            gradeDetailMapper.insert(detail);
        }

        // 总分 = 客观题分 + 主观题分；状态 = 2=已评（AI 完成）
        BigDecimal total = (record.getObjectiveScore() != null ? record.getObjectiveScore() : BigDecimal.ZERO)
                .add(subjectiveScore);
        record.setSubjectiveScore(subjectiveScore);
        record.setTotalScore(total);
        record.setStatus(STATUS_GRADED);
        record.setUpdateBy(request.getUserId());
        gradeRecordMapper.updateById(record);

        BigDecimal passScore = record.getPassScore() != null ? record.getPassScore() : DEFAULT_PASS_SCORE;
        Integer isPassed = total.compareTo(passScore) >= 0 ? 1 : 0;
        record.setIsPassed(isPassed);
        gradeRecordMapper.updateById(record);

        return EvaluationResultVO.builder()
                .gradeId(record.getId())
                .examId(request.getExamId())
                .userId(request.getUserId())
                .totalScore(total)
                .objectiveScore(record.getObjectiveScore())
                .subjectiveScore(subjectiveScore)
                .pendingManualCount(0)
                .isPassed(isPassed)
                .status(STATUS_GRADED)
                .details(details)
                .build();
    }

    /**
     * 构造 AI Prompt。
     */
    private String buildAiPrompt(AiEvaluateRequest.EssayAnswer ea) {
        return String.format(
                "请评阅以下主观题，满分 %d 分。\n\n题干：%s\n\n参考答案：%s\n\n学员答案：%s\n\n" +
                        "请按以下 JSON 格式输出（不要任何其他文字）：\n{\"score\": <0~%d 的整数>, \"comment\": \"<简短评语>\"}",
                ea.getFullScore(), ea.getStem(),
                Objects.toString(ea.getReferenceAnswer(), "（无）"),
                ea.getUserAnswer(), ea.getFullScore());
    }

    /**
     * 解析 AI 响应。W2 阶段 AI 返回的可能是 Mock 占位 JSON，宽松解析。
     */
    private AiScore parseAiResponse(String resp, Integer fullScore) {
        if (resp == null || resp.isBlank()) {
            return new AiScore(BigDecimal.ZERO, "AI 无响应");
        }
        // 尝试抽取 "score": N
        try {
            int idx = resp.indexOf("\"score\"");
            if (idx >= 0) {
                int colon = resp.indexOf(":", idx);
                StringBuilder sb = new StringBuilder();
                for (int i = colon + 1; i < resp.length(); i++) {
                    char c = resp.charAt(i);
                    if (Character.isDigit(c) || c == '.') sb.append(c);
                    else if (sb.length() > 0) break;
                }
                if (sb.length() > 0) {
                    BigDecimal s = new BigDecimal(sb.toString()).setScale(2, RoundingMode.HALF_UP);
                    if (fullScore != null && s.compareTo(BigDecimal.valueOf(fullScore)) > 0) {
                        s = BigDecimal.valueOf(fullScore);
                    }
                    if (s.compareTo(BigDecimal.ZERO) < 0) s = BigDecimal.ZERO;
                    return new AiScore(s, "AI 评阅（Mock）");
                }
            }
        } catch (Exception e) {
            log.warn("[parseAiResponse] failed, fallback to 0", e);
        }
        return new AiScore(BigDecimal.ZERO, "AI 解析失败，待人工评");
    }

    private record AiScore(BigDecimal score, String comment) {}

    // ========== 人工评阅 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EvaluationResultVO manualEvaluate(ManualEvaluateRequest request, Long operatorId) {
        log.info("[manualEvaluate] gradeId={} items={}", request.getGradeId(),
                request.getItems() == null ? 0 : request.getItems().size());

        GradeRecord record = gradeRecordMapper.selectById(request.getGradeId());
        if (record == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "成绩记录不存在");
        }
        if (record.getStatus() != null && record.getStatus() == STATUS_PUBLISHED) {
            throw new BusinessException(ErrorCode.EVALUATION_PUBLISHED, "成绩已发布，不可修改");
        }

        BigDecimal manualDelta = BigDecimal.ZERO;
        List<EvaluationResultVO.QuestionResult> details = new ArrayList<>();

        for (ManualEvaluateRequest.ManualScoreItem item : request.getItems()) {
            // 查找已有明细
            GradeDetail detail = gradeDetailMapper.selectOne(new QueryWrapper<GradeDetail>()
                    .eq("grade_id", record.getId())
                    .eq("question_id", item.getQuestionId())
                    .eq("deleted", 0));

            BigDecimal oldScore = detail != null && detail.getScore() != null ? detail.getScore() : BigDecimal.ZERO;
            BigDecimal newScore = BigDecimal.valueOf(item.getScore() == null ? 0 : item.getScore());
            BigDecimal delta = newScore.subtract(oldScore);

            if (detail == null) {
                detail = new GradeDetail();
                detail.setGradeId(record.getId());
                detail.setQuestionId(item.getQuestionId());
                detail.setFullScore(newScore);
                detail.setCreateBy(operatorId);
            } else {
                detail.setFullScore(newScore);
            }
            detail.setScore(newScore);
            detail.setIsCorrect(item.getIsCorrect() != null ? item.getIsCorrect()
                    : (newScore.compareTo(BigDecimal.ZERO) > 0 ? 1 : 0));
            detail.setEvaluatorType(EVAL_MANUAL);
            detail.setEvaluatorId(operatorId);
            detail.setComment(item.getComment());
            detail.setUpdateBy(operatorId);

            if (detail.getId() == null) {
                gradeDetailMapper.insert(detail);
            } else {
                gradeDetailMapper.updateById(detail);
            }

            manualDelta = manualDelta.add(delta);
            details.add(EvaluationResultVO.QuestionResult.builder()
                    .questionId(item.getQuestionId())
                    .isCorrect(detail.getIsCorrect())
                    .score(newScore)
                    .evaluatorType(EVAL_MANUAL)
                    .comment(item.getComment())
                    .build());
        }

        BigDecimal total = (record.getTotalScore() != null ? record.getTotalScore() : BigDecimal.ZERO).add(manualDelta);
        record.setTotalScore(total);
        record.setStatus(STATUS_GRADED);
        record.setUpdateBy(operatorId);
        BigDecimal passScore = record.getPassScore() != null ? record.getPassScore() : DEFAULT_PASS_SCORE;
        record.setIsPassed(total.compareTo(passScore) >= 0 ? 1 : 0);
        gradeRecordMapper.updateById(record);

        return EvaluationResultVO.builder()
                .gradeId(record.getId())
                .examId(record.getExamId())
                .userId(record.getUserId())
                .totalScore(total)
                .objectiveScore(record.getObjectiveScore())
                .subjectiveScore(record.getSubjectiveScore())
                .pendingManualCount(0)
                .isPassed(record.getIsPassed())
                .status(STATUS_GRADED)
                .details(details)
                .build();
    }

    // ========== 复核 / 发布 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EvaluationActionVO review(Long gradeId, Long operatorId) {
        GradeRecord record = mustGet(gradeId);
        if (record.getStatus() == null || record.getStatus() != STATUS_GRADED) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "仅已评可复核");
        }
        record.setStatus(STATUS_REVIEWED);
        record.setUpdateBy(operatorId);
        gradeRecordMapper.updateById(record);
        return actionVO(record, "复核完成", operatorId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EvaluationActionVO publish(Long gradeId, Long operatorId) {
        GradeRecord record = mustGet(gradeId);
        if (record.getStatus() == null
                || (record.getStatus() != STATUS_GRADED && record.getStatus() != STATUS_REVIEWED)) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "仅已评/已复核可发布");
        }
        record.setStatus(STATUS_PUBLISHED);
        record.setPublishedAt(LocalDateTime.now());
        record.setUpdateBy(operatorId);
        gradeRecordMapper.updateById(record);
        return actionVO(record, "成绩已发布", operatorId);
    }

    // ========== 待评列表 ==========

    @Override
    public PageResult<PendingItemVO> listPending(Long examId, Integer status, int pageNum, int pageSize) {
        QueryWrapper<GradeRecord> qw = new QueryWrapper<>();
        if (examId != null) qw.eq("exam_id", examId);
        if (status != null) {
            qw.eq("status", status);
        } else {
            // 默认查 0/1/2（待评 / 部分 / 待复核）
            qw.in("status", STATUS_PENDING, STATUS_PARTIAL, STATUS_GRADED);
        }
        qw.orderByAsc("submitted_at");
        qw.eq("deleted", 0);

        long total = gradeRecordMapper.selectCount(qw);
        List<GradeRecord> records = gradeRecordMapper.selectList(qw
                .last("LIMIT " + pageSize + " OFFSET " + ((pageNum - 1) * pageSize)));

        List<PendingItemVO> items = new ArrayList<>();
        for (GradeRecord r : records) {
            items.add(PendingItemVO.builder()
                    .gradeId(r.getId())
                    .examId(r.getExamId())
                    .userId(r.getUserId())
                    .status(r.getStatus())
                    .statusLabel(statusLabel(r.getStatus()))
                    .submittedAt(r.getSubmittedAt())
                    .build());
        }
        return new PageResult<>(total, pageNum, pageSize, items);
    }

    @Override
    public EvaluationResultVO getDetail(Long gradeId) {
        GradeRecord record = mustGet(gradeId);
        List<GradeDetail> details = gradeDetailMapper.selectList(new QueryWrapper<GradeDetail>()
                .eq("grade_id", gradeId).eq("deleted", 0));
        List<EvaluationResultVO.QuestionResult> qrs = new ArrayList<>();
        for (GradeDetail d : details) {
            qrs.add(EvaluationResultVO.QuestionResult.builder()
                    .questionId(d.getQuestionId())
                    .isCorrect(d.getIsCorrect())
                    .score(d.getScore())
                    .evaluatorType(d.getEvaluatorType())
                    .comment(d.getComment())
                    .build());
        }
        return EvaluationResultVO.builder()
                .gradeId(record.getId())
                .examId(record.getExamId())
                .userId(record.getUserId())
                .totalScore(record.getTotalScore())
                .objectiveScore(record.getObjectiveScore())
                .subjectiveScore(record.getSubjectiveScore())
                .isPassed(record.getIsPassed())
                .status(record.getStatus())
                .details(qrs)
                .build();
    }

    // ========== 私有 ==========

    private GradeRecord upsertGradeRecord(Long examId, Long userId) {
        GradeRecord existing = gradeRecordMapper.selectOne(new QueryWrapper<GradeRecord>()
                .eq("exam_id", examId)
                .eq("user_id", userId)
                .eq("deleted", 0));
        if (existing == null) {
            GradeRecord r = new GradeRecord();
            r.setExamId(examId);
            r.setUserId(userId);
            r.setStatus(STATUS_PENDING);
            r.setPassScore(DEFAULT_PASS_SCORE);
            r.setSubmittedAt(LocalDateTime.now());
            r.setCreateBy(userId);
            r.setUpdateBy(userId);
            gradeRecordMapper.insert(r);
            return r;
        }
        if (existing.getStatus() != null && existing.getStatus() == STATUS_PUBLISHED) {
            throw new BusinessException(ErrorCode.EVALUATION_PUBLISHED, "成绩已发布，不可重新评卷");
        }
        return existing;
    }

    private void upsertGradeDetail(Long gradeId,
                                    AutoEvaluateRequest.QuestionAnswer qa,
                                    EvaluationResultVO.QuestionResult qr) {
        GradeDetail existing = gradeDetailMapper.selectOne(new QueryWrapper<GradeDetail>()
                .eq("grade_id", gradeId)
                .eq("question_id", qa.getQuestionId())
                .eq("deleted", 0));
        GradeDetail detail = existing != null ? existing : new GradeDetail();
        detail.setGradeId(gradeId);
        detail.setQuestionId(qa.getQuestionId());
        detail.setUserAnswer(qa.getUserAnswer());
        detail.setCorrectAnswer(qa.getCorrectAnswer());
        detail.setIsCorrect(qr.getIsCorrect() == null ? 0 : qr.getIsCorrect());
        detail.setScore(qr.getScore() != null ? qr.getScore() : BigDecimal.ZERO);
        detail.setFullScore(BigDecimal.valueOf(qa.getFullScore() == null ? 0 : qa.getFullScore()));
        detail.setEvaluatorType(EVAL_AUTO);
        detail.setCreateBy(detail.getCreateBy() == null ? CommonConstants.SUPER_ADMIN_ID : detail.getCreateBy());
        detail.setUpdateBy(CommonConstants.SUPER_ADMIN_ID);
        if (existing == null) {
            gradeDetailMapper.insert(detail);
        } else {
            gradeDetailMapper.updateById(detail);
        }
    }

    private GradeRecord mustGet(Long gradeId) {
        GradeRecord record = gradeRecordMapper.selectById(gradeId);
        if (record == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "成绩记录不存在");
        }
        return record;
    }

    private EvaluationActionVO actionVO(GradeRecord r, String msg, Long operatorId) {
        return EvaluationActionVO.builder()
                .gradeId(r.getId())
                .status(r.getStatus())
                .statusLabel(statusLabel(r.getStatus()))
                .actionTime(LocalDateTime.now())
                .operatorId(operatorId)
                .message(msg)
                .build();
    }

    private String statusLabel(Integer status) {
        if (status == null) return "";
        return switch (status) {
            case STATUS_PENDING -> "待评分";
            case STATUS_PARTIAL -> "部分评分";
            case STATUS_GRADED -> "已评（待复核）";
            case STATUS_REVIEWED -> "已复核";
            case STATUS_PUBLISHED -> "已发布";
            default -> "未知";
        };
    }
}
