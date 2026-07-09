package com.gac.lms.module.exam.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gac.lms.common.exception.BusinessException;
import com.gac.lms.module.exam.dto.SaveAllRequest;
import com.gac.lms.module.exam.dto.SaveAnswerRequest;
import com.gac.lms.module.exam.entity.ExamTaking;
import com.gac.lms.module.exam.mapper.ExamTakingMapper;
import com.gac.lms.module.exam.service.ExamTakingService;
import com.gac.lms.module.exam.service.PaperQueryService;
import com.gac.lms.module.exam.vo.PaperRenderVO;
import com.gac.lms.module.exam.vo.RemainingTimeVO;
import com.gac.lms.module.exam.vo.TakingSnapshotVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ExamTakingServiceImpl 单元测试���
 *
 * <p>覆盖核心场景：试卷渲染、暂存乐观锁、批量暂存、快照读取、交卷、自动交卷。</p>
 *
 * @author 方雨菲
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class ExamTakingServiceImplTest {

    @Mock private ExamTakingMapper examTakingMapper;
    @Mock private PaperQueryService paperQueryService;
    @Mock private RedisTemplate<String, Object> redisTemplate;
    @Mock private HashOperations<String, Object, Object> hashOps;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private ExamTakingService service;

    private static final Long EXAM_ID = 1001L;
    private static final Long USER_ID = 8888L;
    private static final Long PAPER_ID = 2001L;
    private static final String REDIS_KEY = "exam:taking:v2:1001:8888";

    @BeforeEach
    void setUp() {
        service = new ExamTakingServiceImpl(examTakingMapper, paperQueryService, redisTemplate, objectMapper);
        // self 字段被 @Lazy 注入，但单测中不会用到，可注入自身
        ReflectionTestUtils.setField(service, "self", service);
        lenient().when(redisTemplate.opsForHash()).thenReturn(hashOps);
    }

    // ========== renderPaper ==========

    @Test
    @DisplayName("renderPaper - 首次进入：初始化快照并返回 version=0")
    void renderPaper_firstEntry_initializesSnapshot() {
        when(paperQueryService.getPaper(EXAM_ID)).thenReturn(mockPaper());
        when(redisTemplate.hasKey(REDIS_KEY)).thenReturn(false);

        PaperRenderVO result = service.renderPaper(EXAM_ID, USER_ID);

        assertThat(result).isNotNull();
        assertThat(result.getPaperId()).isEqualTo(PAPER_ID);
        assertThat(result.getCurrentVersion()).isEqualTo(0);
        verify(hashOps).putAll(eq(REDIS_KEY), any());
        verify(redisTemplate).expire(eq(REDIS_KEY), any());
    }

    @Test
    @DisplayName("renderPaper - 非首次：返回当前 version")
    void renderPaper_reentry_returnsCurrentVersion() {
        when(paperQueryService.getPaper(EXAM_ID)).thenReturn(mockPaper());
        when(redisTemplate.hasKey(REDIS_KEY)).thenReturn(true);
        when(hashOps.get(REDIS_KEY, "version")).thenReturn("5");

        PaperRenderVO result = service.renderPaper(EXAM_ID, USER_ID);

        assertThat(result.getCurrentVersion()).isEqualTo(5);
        verify(hashOps, never()).putAll(eq(REDIS_KEY), any());
    }

    @Test
    @DisplayName("renderPaper - 试卷不存在：抛 BusinessException")
    void renderPaper_paperNotFound_throws() {
        when(paperQueryService.getPaper(EXAM_ID)).thenReturn(null);

        assertThatThrownBy(() -> service.renderPaper(EXAM_ID, USER_ID))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("试卷不存在");
    }

    // ========== saveAnswer ==========

    @Test
    @DisplayName("saveAnswer - 版本一致：执行 Lua 脚本并返回新版本号")
    void saveAnswer_versionMatch_returnsNewVersion() {
        when(redisTemplate.hasKey(REDIS_KEY)).thenReturn(true);
        when(hashOps.get(REDIS_KEY, "version")).thenReturn("3");
        when(redisTemplate.execute(any(org.springframework.data.redis.core.script.RedisScript.class),
                any(java.util.List.class), any(Object[].class))).thenReturn("4");

        SaveAnswerRequest req = new SaveAnswerRequest();
        req.setQuestionId(10L);
        req.setAnswer("A");
        req.setVersion(3);

        Integer newVer = service.saveAnswer(EXAM_ID, USER_ID, req);

        assertThat(newVer).isEqualTo(4);
    }

    @Test
    @DisplayName("saveAnswer - 版本冲突：抛 ANSWER_SAVE_CONFLICT")
    void saveAnswer_versionConflict_throws() {
        when(redisTemplate.hasKey(REDIS_KEY)).thenReturn(true);
        when(hashOps.get(REDIS_KEY, "version")).thenReturn("3");

        SaveAnswerRequest req = new SaveAnswerRequest();
        req.setQuestionId(10L);
        req.setAnswer("A");
        req.setVersion(2); // 客户端版本号 = 2，服务端 = 3，冲突

        assertThatThrownBy(() -> service.saveAnswer(EXAM_ID, USER_ID, req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("答题版本冲突");

        verify(redisTemplate, never()).execute(any(org.springframework.data.redis.core.script.RedisScript.class),
                any(java.util.List.class), any(Object[].class));
    }

    @Test
    @DisplayName("saveAnswer - 快照不存在：抛 OPERATION_NOT_ALLOWED")
    void saveAnswer_snapshotMissing_throws() {
        when(redisTemplate.hasKey(REDIS_KEY)).thenReturn(false);

        SaveAnswerRequest req = new SaveAnswerRequest();
        req.setQuestionId(10L);
        req.setAnswer("A");
        req.setVersion(0);

        assertThatThrownBy(() -> service.saveAnswer(EXAM_ID, USER_ID, req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("请先进入考试");
    }

    // ========== saveAll ==========

    @Test
    @DisplayName("saveAll - 版本一致：批量写入并自增 version")
    void saveAll_versionMatch_writesAllAnswers() {
        when(redisTemplate.hasKey(REDIS_KEY)).thenReturn(true);
        when(hashOps.get(REDIS_KEY, "version")).thenReturn("2");
        when(hashOps.increment(REDIS_KEY, "version", 1L)).thenReturn(3L);

        Map<Long, String> answers = new HashMap<>();
        answers.put(1L, "A");
        answers.put(2L, "B");
        SaveAllRequest req = new SaveAllRequest();
        req.setAnswers(answers);
        req.setVersion(2);

        Integer newVer = service.saveAll(EXAM_ID, USER_ID, req);

        assertThat(newVer).isEqualTo(3);
        verify(hashOps).putAll(eq(REDIS_KEY), any());
        verify(hashOps).increment(REDIS_KEY, "version", 1L);
    }

    @Test
    @DisplayName("saveAll - 版本冲突：抛异常不写入")
    void saveAll_versionConflict_throws() {
        when(redisTemplate.hasKey(REDIS_KEY)).thenReturn(true);
        when(hashOps.get(REDIS_KEY, "version")).thenReturn("5");

        SaveAllRequest req = new SaveAllRequest();
        req.setAnswers(Map.of(1L, "A"));
        req.setVersion(3);

        assertThatThrownBy(() -> service.saveAll(EXAM_ID, USER_ID, req))
                .isInstanceOf(BusinessException.class);

        verify(hashOps, never()).putAll(eq(REDIS_KEY), any());
    }

    // ========== getSnapshot ==========

    @Test
    @DisplayName("getSnapshot - 快照丢失：返回空快照（不抛异常）")
    void getSnapshot_snapshotMissing_returnsEmpty() {
        when(redisTemplate.hasKey(REDIS_KEY)).thenReturn(false);

        TakingSnapshotVO snapshot = service.getSnapshot(EXAM_ID, USER_ID);

        assertThat(snapshot.getVersion()).isEqualTo(0);
        assertThat(snapshot.getAnswers()).isEmpty();
        assertThat(snapshot.getAnsweredCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("getSnapshot - 正常快照：正确解析题目与版本")
    void getSnapshot_normalSnapshot_parsesAnswers() {
        when(redisTemplate.hasKey(REDIS_KEY)).thenReturn(true);
        Map<Object, Object> entries = new HashMap<>();
        entries.put("version", "5");
        entries.put("paperId", String.valueOf(PAPER_ID));
        entries.put("startTime", LocalDateTime.now().toString());
        entries.put("1", "A");
        entries.put("2", "B");
        when(hashOps.entries(REDIS_KEY)).thenReturn(entries);
        when(paperQueryService.getPaperQuestionCount(PAPER_ID)).thenReturn(5);

        TakingSnapshotVO snapshot = service.getSnapshot(EXAM_ID, USER_ID);

        assertThat(snapshot.getVersion()).isEqualTo(5);
        assertThat(snapshot.getPaperId()).isEqualTo(PAPER_ID);
        assertThat(snapshot.getAnswers()).containsEntry(1L, "A").containsEntry(2L, "B");
        assertThat(snapshot.getAnsweredCount()).isEqualTo(2);
        assertThat(snapshot.getTotalCount()).isEqualTo(5);
    }

    // ========== submit ==========

    @Test
    @DisplayName("submit - 首次交卷：insert + delete Redis")
    void submit_firstTime_insertsAndClearsRedis() {
        // 模拟快照存在
        when(redisTemplate.hasKey(REDIS_KEY)).thenReturn(true);
        Map<Object, Object> entries = new HashMap<>();
        entries.put("version", "3");
        entries.put("paperId", String.valueOf(PAPER_ID));
        entries.put("startTime", LocalDateTime.now().minusMinutes(30).toString());
        entries.put("1", "A");
        when(hashOps.entries(REDIS_KEY)).thenReturn(entries);
        when(paperQueryService.getPaperQuestionCount(PAPER_ID)).thenReturn(5);

        // 模拟 DB 无已有记录
        when(examTakingMapper.selectOne(any())).thenReturn(null);
        when(examTakingMapper.insert(any(ExamTaking.class))).thenReturn(1);

        var result = service.submit(EXAM_ID, USER_ID);

        assertThat(result.getExamId()).isEqualTo(EXAM_ID);
        assertThat(result.getAnsweredCount()).isEqualTo(1);
        verify(examTakingMapper).insert(any(ExamTaking.class));
        verify(redisTemplate).delete(REDIS_KEY);
    }

    @Test
    @DisplayName("submit - 二次交卷：update + delete Redis")
    void submit_reSubmit_updates() {
        ExamTaking existing = new ExamTaking();
        existing.setId(123L);
        existing.setExamId(EXAM_ID);
        existing.setUserId(USER_ID);

        when(redisTemplate.hasKey(REDIS_KEY)).thenReturn(true);
        Map<Object, Object> entries = new HashMap<>();
        entries.put("version", "5");
        entries.put("paperId", String.valueOf(PAPER_ID));
        entries.put("startTime", LocalDateTime.now().minusMinutes(10).toString());
        when(hashOps.entries(REDIS_KEY)).thenReturn(entries);
        when(paperQueryService.getPaperQuestionCount(PAPER_ID)).thenReturn(5);

        when(examTakingMapper.selectOne(any())).thenReturn(existing);
        when(examTakingMapper.updateById(any(ExamTaking.class))).thenReturn(1);

        var result = service.submit(EXAM_ID, USER_ID);

        ArgumentCaptor<ExamTaking> captor = ArgumentCaptor.forClass(ExamTaking.class);
        verify(examTakingMapper).updateById(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(123L);
        assertThat(captor.getValue().getStatus()).isEqualTo(1);
        assertThat(result.getTakingId()).isEqualTo(123L);
    }

    @Test
    @DisplayName("submit - 快照不存在：抛 OPERATION_NOT_ALLOWED")
    void submit_snapshotMissing_throws() {
        when(redisTemplate.hasKey(REDIS_KEY)).thenReturn(false);

        assertThatThrownBy(() -> service.submit(EXAM_ID, USER_ID))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("无进行中的作答");

        verify(examTakingMapper, never()).insert(any(ExamTaking.class));
    }

    @Test
    @DisplayName("autoSubmit - 调用 doSubmit 并标记为自动")
    void autoSubmit_invokesDoSubmit() {
        when(redisTemplate.hasKey(REDIS_KEY)).thenReturn(false);
        assertThatThrownBy(() -> service.autoSubmit(EXAM_ID, USER_ID))
                .isInstanceOf(BusinessException.class);
    }

    // ========== getRemaining ==========

    @Test
    @DisplayName("getRemaining - 未开始：remaining=0 overtime=true")
    void getRemaining_noSnapshot_returnsZero() {
        when(redisTemplate.hasKey(REDIS_KEY)).thenReturn(false);

        RemainingTimeVO vo = service.getRemaining(EXAM_ID, USER_ID);

        assertThat(vo.getRemainingSeconds()).isEqualTo(0L);
        assertThat(vo.getOvertime()).isTrue();
    }

    @Test
    @DisplayName("getRemaining - 考试中：返回剩余秒数")
    void getRemaining_inProgress_returnsSeconds() {
        when(redisTemplate.hasKey(REDIS_KEY)).thenReturn(true);
        when(hashOps.get(REDIS_KEY, "startTime")).thenReturn(LocalDateTime.now().minusMinutes(10).toString());
        when(hashOps.get(REDIS_KEY, "paperId")).thenReturn(String.valueOf(PAPER_ID));
        PaperRenderVO paper = mockPaper();
        when(paperQueryService.getPaper(PAPER_ID)).thenReturn(paper);

        RemainingTimeVO vo = service.getRemaining(EXAM_ID, USER_ID);

        // 60 分钟 - 10 分钟 = 50 分钟 = 3000 秒
        assertThat(vo.getRemainingSeconds()).isBetween(2900L, 3010L);
        assertThat(vo.getOvertime()).isFalse();
    }

    // ========== 私有方法辅助 ==========

    private PaperRenderVO mockPaper() {
        PaperRenderVO paper = new PaperRenderVO();
        paper.setExamId(EXAM_ID);
        paper.setPaperId(PAPER_ID);
        paper.setTitle("Mock Paper");
        paper.setStartTime(LocalDateTime.now());
        paper.setEndTime(LocalDateTime.now().plusHours(1));
        paper.setDurationMinutes(60);
        paper.setTotalScore(100);
        return paper;
    }
}
