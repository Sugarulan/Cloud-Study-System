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
import com.gac.lms.module.selfTest.vo.ExamItemVO;
import com.gac.lms.module.selfTest.vo.WrongQuestionVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * SelfTestServiceImpl 单元测试。
 *
 * @author 方雨菲
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class SelfTestServiceImplTest {

    @Mock private WrongQuestionMapper wrongQuestionMapper;
    @Mock private GradeRecordViewMapper gradeRecordViewMapper;
    @Mock private LLMService llmService;

    private SelfTestServiceImpl service;

    private static final Long USER_ID = 8888L;

    @BeforeEach
    void setUp() {
        service = new SelfTestServiceImpl(wrongQuestionMapper, gradeRecordViewMapper, llmService);
    }

    // ========== listPendingExams ==========

    @Test
    @DisplayName("listPendingExams - W2 占位：返回空（待 W3 联调）")
    void listPendingExams_w2_returnsEmpty() {
        PageResult<ExamItemVO> page = service.listPendingExams(USER_ID, 1, 20);
        assertThat(page.getTotal()).isEqualTo(0L);
        assertThat(page.getRecords()).isEmpty();
    }

    // ========== listFinishedExams ==========

    @Test
    @DisplayName("listFinishedExams - 仅返回已发布且包含错题数")
    void listFinishedExams_success() {
        when(gradeRecordViewMapper.selectCount(any(QueryWrapper.class))).thenReturn(2L);

        GradeRecordView r1 = new GradeRecordView();
        r1.setId(100L);
        r1.setExamId(1001L);
        r1.setPaperId(1L);
        r1.setTotalScore(new BigDecimal("85"));
        r1.setIsPassed(1);
        r1.setStatus(4);
        r1.setSubmittedAt(LocalDateTime.now());
        GradeRecordView r2 = new GradeRecordView();
        r2.setId(101L);
        r2.setExamId(1002L);
        r2.setTotalScore(new BigDecimal("55"));
        r2.setIsPassed(0);
        r2.setStatus(4);
        when(gradeRecordViewMapper.selectList(any(QueryWrapper.class)))
                .thenReturn(Arrays.asList(r1, r2));

        // 错题数统��
        WrongQuestion wq1 = new WrongQuestion();
        wq1.setGradeId(100L);
        WrongQuestion wq2 = new WrongQuestion();
        wq2.setGradeId(100L);
        WrongQuestion wq3 = new WrongQuestion();
        wq3.setGradeId(101L);
        when(wrongQuestionMapper.selectList(any(QueryWrapper.class)))
                .thenReturn(Arrays.asList(wq1, wq2, wq3));

        PageResult<ExamItemVO> page = service.listFinishedExams(USER_ID, 1, 20);

        assertThat(page.getTotal()).isEqualTo(2L);
        assertThat(page.getRecords()).hasSize(2);
        assertThat(page.getRecords().get(0).getGradeId()).isEqualTo(100L);
        assertThat(page.getRecords().get(0).getWrongCount()).isEqualTo(2);
        assertThat(page.getRecords().get(1).getWrongCount()).isEqualTo(1);
        assertThat(page.getRecords().get(0).getIsPassed()).isEqualTo(1);
        assertThat(page.getRecords().get(1).getIsPassed()).isEqualTo(0);
        assertThat(page.getRecords().get(0).getStatusLabel()).matches(".*发布.*");
    }

    @Test
    @DisplayName("listFinishedExams - 空结果")
    void listFinishedExams_empty() {
        when(gradeRecordViewMapper.selectCount(any(QueryWrapper.class))).thenReturn(0L);
        when(gradeRecordViewMapper.selectList(any(QueryWrapper.class))).thenReturn(Collections.emptyList());

        PageResult<ExamItemVO> page = service.listFinishedExams(USER_ID, 1, 20);

        assertThat(page.getRecords()).isEmpty();
        // 错题统计不应该被调用（gradeIds 为空）
        verify(wrongQuestionMapper, never()).selectList(any(QueryWrapper.class));
    }

    // ========== listWrongQuestions ==========

    @Test
    @DisplayName("listWrongQuestions - 默认（全部）")
    void listWrongQuestions_all() {
        when(wrongQuestionMapper.selectCount(any(QueryWrapper.class))).thenReturn(2L);

        WrongQuestion w1 = new WrongQuestion();
        w1.setId(1L);
        w1.setUserId(USER_ID);
        w1.setQuestionId(100L);
        w1.setUserAnswer("A");
        w1.setCorrectAnswer("B");
        w1.setIsMastered(0);
        WrongQuestion w2 = new WrongQuestion();
        w2.setId(2L);
        w2.setUserId(USER_ID);
        w2.setQuestionId(101L);
        w2.setIsMastered(1);
        when(wrongQuestionMapper.selectList(any(QueryWrapper.class))).thenReturn(Arrays.asList(w1, w2));

        PageResult<WrongQuestionVO> page = service.listWrongQuestions(USER_ID, null, 1, 20);

        assertThat(page.getTotal()).isEqualTo(2L);
        assertThat(page.getRecords()).hasSize(2);
        assertThat(page.getRecords().get(0).getId()).isEqualTo(1L);
        assertThat(page.getRecords().get(0).getUserAnswer()).isEqualTo("A");
    }

    @Test
    @DisplayName("listWrongQuestions - 只看未掌握")
    void listWrongQuestions_onlyUnmastered() {
        when(wrongQuestionMapper.selectCount(any(QueryWrapper.class))).thenReturn(1L);

        WrongQuestion w1 = new WrongQuestion();
        w1.setId(1L);
        w1.setUserId(USER_ID);
        w1.setIsMastered(0);
        when(wrongQuestionMapper.selectList(any(QueryWrapper.class)))
                .thenReturn(Collections.singletonList(w1));

        PageResult<WrongQuestionVO> page = service.listWrongQuestions(USER_ID, 0, 1, 20);

        assertThat(page.getRecords()).hasSize(1);
        assertThat(page.getRecords().get(0).getIsMastered()).isEqualTo(0);
    }

    // ========== getWrongQuestion ==========

    @Test
    @DisplayName("getWrongQuestion - 正常返回")
    void getWrongQuestion_success() {
        WrongQuestion w = new WrongQuestion();
        w.setId(1L);
        w.setUserId(USER_ID);
        w.setQuestionId(100L);
        w.setUserAnswer("A");
        w.setCorrectAnswer("B");
        w.setAiExplanation("AI 解析示例");
        when(wrongQuestionMapper.selectById(1L)).thenReturn(w);

        WrongQuestionVO vo = service.getWrongQuestion(1L, USER_ID);

        assertThat(vo.getId()).isEqualTo(1L);
        assertThat(vo.getAiExplanation()).isEqualTo("AI 解析示例");
    }

    @Test
    @DisplayName("getWrongQuestion - 不存在：抛 DATA_NOT_FOUND")
    void getWrongQuestion_notFound() {
        when(wrongQuestionMapper.selectById(999L)).thenReturn(null);
        assertThatThrownBy(() -> service.getWrongQuestion(999L, USER_ID))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("getWrongQuestion - 越权访问别人的错题：抛 FORBIDDEN")
    void getWrongQuestion_otherUser() {
        WrongQuestion w = new WrongQuestion();
        w.setId(1L);
        w.setUserId(9999L); // 别人的错题
        when(wrongQuestionMapper.selectById(1L)).thenReturn(w);

        assertThatThrownBy(() -> service.getWrongQuestion(1L, USER_ID))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("无权访问");
    }

    // ========== triggerAiExplanation ==========

    @Test
    @DisplayName("triggerAiExplanation - 缓存命中（24h 内已解析）：直接返回")
    void triggerAi_cacheHit() {
        WrongQuestion w = new WrongQuestion();
        w.setId(1L);
        w.setUserId(USER_ID);
        w.setAiExplanation("已解析的内容");
        w.setAiExplainedAt(LocalDateTime.now().minusHours(2));
        when(wrongQuestionMapper.selectById(1L)).thenReturn(w);

        WrongQuestionVO vo = service.triggerAiExplanation(1L, USER_ID);

        assertThat(vo.getAiExplanation()).isEqualTo("已解析的内容");
        verify(llmService, never()).invokeSafely(anyString());
    }

    @Test
    @DisplayName("triggerAiExplanation - 调用 AI 并回写")
    void triggerAi_callAiAndPersist() {
        WrongQuestion w = new WrongQuestion();
        w.setId(1L);
        w.setUserId(USER_ID);
        w.setQuestionId(100L);
        w.setUserAnswer("A");
        w.setCorrectAnswer("B");
        // 无 AI 解析
        when(wrongQuestionMapper.selectById(1L)).thenReturn(w);
        when(llmService.invokeSafely(anyString())).thenReturn("详细解析...");
        when(wrongQuestionMapper.updateById(any(WrongQuestion.class))).thenReturn(1);

        WrongQuestionVO vo = service.triggerAiExplanation(1L, USER_ID);

        assertThat(vo.getAiExplanation()).isEqualTo("详细解析...");
        assertThat(vo.getAiExplainedAt()).isNotNull();
        verify(wrongQuestionMapper).updateById(any(WrongQuestion.class));
    }

    @Test
    @DisplayName("triggerAiExplanation - AI 返回空：使用兜底文���")
    void triggerAi_emptyResponse_fallbackText() {
        WrongQuestion w = new WrongQuestion();
        w.setId(1L);
        w.setUserId(USER_ID);
        w.setQuestionId(100L);
        when(wrongQuestionMapper.selectById(1L)).thenReturn(w);
        when(llmService.invokeSafely(anyString())).thenReturn("");

        WrongQuestionVO vo = service.triggerAiExplanation(1L, USER_ID);

        assertThat(vo.getAiExplanation()).contains("暂未返回");
    }

    @Test
    @DisplayName("triggerAiExplanation - AI 抛异常：业务码 AI_INVOKE_FAIL")
    void triggerAi_throws() {
        WrongQuestion w = new WrongQuestion();
        w.setId(1L);
        w.setUserId(USER_ID);
        when(wrongQuestionMapper.selectById(1L)).thenReturn(w);
        when(llmService.invokeSafely(anyString()))
                .thenThrow(new BusinessException(ErrorCode.AI_INVOKE_FAIL));

        assertThatThrownBy(() -> service.triggerAiExplanation(1L, USER_ID))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("AI 解析失败");
    }

    // ========== markAsMastered ==========

    @Test
    @DisplayName("markAsMastered - 标记成功")
    void markAsMastered_success() {
        WrongQuestion w = new WrongQuestion();
        w.setId(1L);
        w.setUserId(USER_ID);
        w.setIsMastered(0);
        when(wrongQuestionMapper.selectById(1L)).thenReturn(w);
        when(wrongQuestionMapper.updateById(any(WrongQuestion.class))).thenReturn(1);

        service.markAsMastered(1L, USER_ID);

        assertThat(w.getIsMastered()).isEqualTo(1);
        verify(wrongQuestionMapper).updateById(any(WrongQuestion.class));
    }

    @Test
    @DisplayName("markAsMastered - 越权：抛 FORBIDDEN")
    void markAsMastered_otherUser() {
        WrongQuestion w = new WrongQuestion();
        w.setId(1L);
        w.setUserId(9999L);
        when(wrongQuestionMapper.selectById(1L)).thenReturn(w);

        assertThatThrownBy(() -> service.markAsMastered(1L, USER_ID))
                .isInstanceOf(BusinessException.class);
    }
}
