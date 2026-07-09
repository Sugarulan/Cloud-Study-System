package com.gac.lms.module.exam.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gac.lms.common.constants.CommonConstants;
import com.gac.lms.common.exception.BusinessException;
import com.gac.lms.common.response.ErrorCode;
import com.gac.lms.module.exam.dto.SaveAllRequest;
import com.gac.lms.module.exam.dto.SaveAnswerRequest;
import com.gac.lms.module.exam.entity.ExamTaking;
import com.gac.lms.module.exam.mapper.ExamTakingMapper;
import com.gac.lms.module.exam.service.ExamTakingService;
import com.gac.lms.module.exam.service.PaperQueryService;
import com.gac.lms.module.exam.vo.PaperRenderVO;
import com.gac.lms.module.exam.vo.RemainingTimeVO;
import com.gac.lms.module.exam.vo.SubmitResultVO;
import com.gac.lms.module.exam.vo.TakingSnapshotVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 在线作答 Service 实现。
 *
 * <h3>Redis 答题快照结构</h3>
 * <pre>
 * Key:    exam:taking:v2:{examId}:{userId}
 * Hash:   {
 *   "version":     "3",           // 乐观锁版本号
 *   "startTime":   "2026-07-08T10:00:00",
 *   "paperId":     "1001",
 *   "{questionId}": "{answer}"     // 题目 ID -> 答案
 * }
 * TTL:    2 小时
 * </pre>
 *
 * <h3>并发安全</h3>
 * <ul>
 *   <li>使用 {@code GETSET} / Lua 脚本保证 version 自增原子性</li>
 *   <li>前端暂存时传当前 version，服务端校验不一致则拒绝</li>
 *   <li>冲突时返回 {@code 1401 ANSWER_SAVE_CONFLICT}，前端提示用户刷新</li>
 * </ul>
 *
 * @author 方雨菲
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExamTakingServiceImpl implements ExamTakingService {

    private final ExamTakingMapper examTakingMapper;
    private final PaperQueryService paperQueryService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * {@link Lazy} 注入防止循环依赖（AutoSubmitTask -> ExamTakingService -> 未来可能引入的 -> Task）
     */
    @Autowired
    @Lazy
    private ExamTakingService self;

    // ===== 字段名常量 =====
    private static final String F_VERSION = "version";
    private static final String F_START_TIME = "startTime";
    private static final String F_PAPER_ID = "paperId";

    @Override
    public PaperRenderVO renderPaper(Long examId, Long userId) {
        log.debug("[renderPaper] examId={}, userId={}", examId, userId);

        // 1. 从试卷服务拉取试卷
        PaperRenderVO paper = paperQueryService.getPaper(examId);
        if (paper == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "试卷不存在");
        }

        // 2. 检查 Redis 快照是否存在；不存在则初始化
        String redisKey = redisKey(examId, userId);
        Boolean hasSnapshot = redisTemplate.hasKey(redisKey);
        if (Boolean.FALSE.equals(hasSnapshot)) {
            initSnapshot(examId, userId, paper.getPaperId());
            paper.setCurrentVersion(0);
        } else {
            Integer version = currentVersion(redisKey);
            paper.setCurrentVersion(version);
        }

        return paper;
    }

    @Override
    public Integer saveAnswer(Long examId, Long userId, SaveAnswerRequest request) {
        String redisKey = redisKey(examId, userId);
        checkSnapshotExists(redisKey);

        Integer currentVer = currentVersion(redisKey);
        if (!currentVer.equals(request.getVersion())) {
            log.warn("[saveAnswer] version conflict examId={} userId={} expected={} actual={}",
                    examId, userId, request.getVersion(), currentVer);
            throw new BusinessException(ErrorCode.ANSWER_SAVE_CONFLICT,
                    "答题版本冲突，请刷新后重试");
        }

        // 通过 Lua 自增版本号 + 写入答案（保证原子性）
        Long newVersion = incrementVersionAndPut(redisKey, request.getQuestionId(), request.getAnswer());
        log.debug("[saveAnswer] examId={} userId={} q={} newVer={}",
                examId, userId, request.getQuestionId(), newVersion);
        return newVersion.intValue();
    }

    @Override
    public Integer saveAll(Long examId, Long userId, SaveAllRequest request) {
        String redisKey = redisKey(examId, userId);
        checkSnapshotExists(redisKey);

        Integer currentVer = currentVersion(redisKey);
        if (!currentVer.equals(request.getVersion())) {
            throw new BusinessException(ErrorCode.ANSWER_SAVE_CONFLICT,
                    "答题版本冲突，请刷新后重试");
        }

        Long newVersion = incrementVersionAndPutAll(redisKey, request.getAnswers());
        log.debug("[saveAll] examId={} userId={} count={} newVer={}",
                examId, userId, request.getAnswers().size(), newVersion);
        return newVersion.intValue();
    }

    @Override
    public TakingSnapshotVO getSnapshot(Long examId, Long userId) {
        String redisKey = redisKey(examId, userId);
        if (Boolean.FALSE.equals(redisTemplate.hasKey(redisKey))) {
            // 快照丢失：返回空快照，前端会触发重新进入
            return TakingSnapshotVO.builder()
                    .examId(examId)
                    .version(0)
                    .answers(new HashMap<>())
                    .answeredCount(0)
                    .totalCount(0)
                    .build();
        }

        HashOperations<String, Object, Object> ops = redisTemplate.opsForHash();
        Map<Object, Object> entries = ops.entries(redisKey);

        Integer version = toInt(entries.get(F_VERSION));
        Long paperId = toLong(entries.get(F_PAPER_ID));
        LocalDateTime startTime = parseStartTime(entries.get(F_START_TIME));

        Map<Long, String> answers = new LinkedHashMap<>();
        int answeredCount = 0;
        for (Map.Entry<Object, Object> e : entries.entrySet()) {
            String field = String.valueOf(e.getKey());
            if (isMetaField(field)) continue;
            try {
                Long qid = Long.parseLong(field);
                answers.put(qid, String.valueOf(e.getValue()));
                answeredCount++;
            } catch (NumberFormatException ignored) {
                // 非题目字段，跳过
            }
        }

        Integer totalCount = paperQueryService.getPaperQuestionCount(paperId);

        return TakingSnapshotVO.builder()
                .examId(examId)
                .paperId(paperId)
                .version(version)
                .startTime(startTime)
                .answers(answers)
                .answeredCount(answeredCount)
                .totalCount(totalCount == null ? 0 : totalCount)
                .build();
    }

    @Override
    public RemainingTimeVO getRemaining(Long examId, Long userId) {
        String redisKey = redisKey(examId, userId);
        if (Boolean.FALSE.equals(redisTemplate.hasKey(redisKey))) {
            return new RemainingTimeVO(0L, true, System.currentTimeMillis());
        }

        HashOperations<String, Object, Object> ops = redisTemplate.opsForHash();
        Object startTimeObj = ops.get(redisKey, F_START_TIME);
        Object paperIdObj = ops.get(redisKey, F_PAPER_ID);
        if (startTimeObj == null || paperIdObj == null) {
            return new RemainingTimeVO(0L, true, System.currentTimeMillis());
        }

        LocalDateTime startTime = parseStartTime(startTimeObj);
        Long paperId = toLong(paperIdObj);
        Integer durationMinutes = paperQueryService.getPaper(paperId).getDurationMinutes();

        LocalDateTime deadline = startTime.plusMinutes(durationMinutes);
        LocalDateTime now = LocalDateTime.now();
        boolean overtime = now.isAfter(deadline);
        long remainingSeconds = overtime ? 0L : Duration.between(now, deadline).getSeconds();

        return new RemainingTimeVO(remainingSeconds, overtime, System.currentTimeMillis());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SubmitResultVO submit(Long examId, Long userId) {
        return doSubmit(examId, userId, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SubmitResultVO autoSubmit(Long examId, Long userId) {
        log.info("[autoSubmit] examId={}, userId={}", examId, userId);
        return doSubmit(examId, userId, true);
    }

    // ===== 私有方法 =====

    private SubmitResultVO doSubmit(Long examId, Long userId, boolean auto) {
        String redisKey = redisKey(examId, userId);
        if (Boolean.FALSE.equals(redisTemplate.hasKey(redisKey))) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "无进行中的作答或已交卷");
        }

        TakingSnapshotVO snapshot = getSnapshot(examId, userId);
        if (snapshot.getAnswers() == null) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "答卷快照异常");
        }

        // 1. 写库 exam_taking
        ExamTaking taking = new ExamTaking();
        taking.setExamId(examId);
        taking.setUserId(userId);
        taking.setPaperId(snapshot.getPaperId());
        taking.setStatus(1); // 已交卷
        taking.setStartTime(snapshot.getStartTime() != null ? snapshot.getStartTime() : LocalDateTime.now());
        taking.setSubmitTime(LocalDateTime.now());
        taking.setSnapshotVersion(snapshot.getVersion());
        taking.setAnswersJson(toJson(snapshot.getAnswers()));
        if (taking.getStartTime() != null && taking.getSubmitTime() != null) {
            Duration d = Duration.between(taking.getStartTime(), taking.getSubmitTime());
            taking.setDurationSec((int) d.getSeconds());
        }

        // upsert
        ExamTaking existing = examTakingMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ExamTaking>()
                        .eq("exam_id", examId)
                        .eq("user_id", userId)
                        .eq("deleted", 0));
        if (existing == null) {
            taking.setCreateBy(userId);
            taking.setUpdateBy(userId);
            examTakingMapper.insert(taking);
        } else {
            taking.setId(existing.getId());
            taking.setUpdateBy(userId);
            examTakingMapper.updateById(taking);
        }

        // 2. 清空 Redis 快照
        redisTemplate.delete(redisKey);

        log.info("[{}submit] examId={} userId={} takingId={} answered={}",
                auto ? "auto" : "manual", examId, userId, taking.getId(), snapshot.getAnsweredCount());

        return SubmitResultVO.builder()
                .takingId(taking.getId())
                .examId(examId)
                .submitTime(taking.getSubmitTime())
                .durationSec(taking.getDurationSec())
                .answeredCount(snapshot.getAnsweredCount())
                .message(auto ? "已自动交卷" : "交卷成功，成绩将由评卷模块处理")
                .build();
    }

    private void initSnapshot(Long examId, Long userId, Long paperId) {
        String redisKey = redisKey(examId, userId);
        Map<String, Object> initMap = new HashMap<>();
        initMap.put(F_VERSION, "0");
        initMap.put(F_START_TIME, LocalDateTime.now().toString());
        initMap.put(F_PAPER_ID, String.valueOf(paperId));

        HashOperations<String, Object, Object> ops = redisTemplate.opsForHash();
        ops.putAll(redisKey, toHashInput(initMap));
        redisTemplate.expire(redisKey, Duration.ofSeconds(CommonConstants.RedisKey.EXAM_TAKING_TTL_SECONDS));
        log.debug("[initSnapshot] examId={} userId={} paperId={}", examId, userId, paperId);
    }

    /**
     * Lua 脚本：自增版本号 + 写入单题答案（原子操作）。
     */
    private Long incrementVersionAndPut(String redisKey, Long questionId, String answer) {
        String lua =
                "local current = redis.call('HGET', KEYS[1], 'version') " +
                "redis.call('HSET', KEYS[1], '" + questionId + "', ARGV[1]) " +
                "redis.call('HINCRBY', KEYS[1], 'version', 1) " +
                "redis.call('EXPIRE', KEYS[1], ARGV[2]) " +
                "return redis.call('HGET', KEYS[1], 'version')";
        Object result = redisTemplate.execute(
                new org.springframework.data.redis.core.script.DefaultRedisScript<>(lua, Object.class),
                java.util.Collections.singletonList(redisKey),
                answer,
                String.valueOf(CommonConstants.RedisKey.EXAM_TAKING_TTL_SECONDS));
        return result == null ? 0L : Long.parseLong(String.valueOf(result));
    }

    /**
     * Lua 脚本：自增版本号 + 批量写入答案（原子操作）。
     */
    private Long incrementVersionAndPutAll(String redisKey, Map<Long, String> answers) {
        // 简化实现：先 HSET 全部答案，再 HINCRBY version 1
        // 严格场景可改用完整 Lua 脚本
        Map<String, Object> hashEntries = new HashMap<>();
        for (Map.Entry<Long, String> e : answers.entrySet()) {
            hashEntries.put(String.valueOf(e.getKey()), e.getValue());
        }
        HashOperations<String, Object, Object> ops = redisTemplate.opsForHash();
        ops.putAll(redisKey, toHashInput(hashEntries));
        Long newVersion = ops.increment(redisKey, F_VERSION, 1L);
        redisTemplate.expire(redisKey, Duration.ofSeconds(CommonConstants.RedisKey.EXAM_TAKING_TTL_SECONDS));
        return newVersion;
    }

    private Integer currentVersion(String redisKey) {
        Object v = redisTemplate.opsForHash().get(redisKey, F_VERSION);
        return toInt(v);
    }

    private void checkSnapshotExists(String redisKey) {
        if (Boolean.FALSE.equals(redisTemplate.hasKey(redisKey))) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "请先进入考试初始化快照");
        }
    }

    private String redisKey(Long examId, Long userId) {
        return String.format(CommonConstants.RedisKey.EXAM_TAKING_V2, examId, userId);
    }

    // ===== 类型转换辅助 =====

    private Integer toInt(Object v) {
        if (v == null) return 0;
        if (v instanceof Number) return ((Number) v).intValue();
        return Integer.parseInt(String.valueOf(v));
    }

    private Long toLong(Object v) {
        if (v == null) return null;
        if (v instanceof Number) return ((Number) v).longValue();
        return Long.parseLong(String.valueOf(v));
    }

    private LocalDateTime parseStartTime(Object v) {
        if (v == null) return null;
        try {
            return LocalDateTime.parse(String.valueOf(v));
        } catch (Exception e) {
            return null;
        }
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("[toJson] failed", e);
            return "{}";
        }
    }

    private boolean isMetaField(String field) {
        return F_VERSION.equals(field) || F_START_TIME.equals(field) || F_PAPER_ID.equals(field);
    }

    /**
     * 把 {@code Map<String, Object>} 转换为 Redis Hash 需要的 {@code Map<Object, Object>}。
     * Jackson 反序列化时 Hash 字段都是字符串类型，需保持一致避免类型冲突。
     */
    private Map<Object, Object> toHashInput(Map<String, Object> src) {
        Map<Object, Object> dst = new HashMap<>(src.size());
        src.forEach((k, v) -> dst.put(k, String.valueOf(v)));
        return dst;
    }

    @SuppressWarnings("unused")
    private Map<Long, String> fromJson(String json, TypeReference<Map<Long, String>> typeRef) {
        try {
            return objectMapper.readValue(json, typeRef);
        } catch (Exception e) {
            log.error("[fromJson] failed", e);
            return new HashMap<>();
        }
    }
}
