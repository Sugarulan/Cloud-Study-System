package com.gac.lms.module.evaluation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gac.lms.ai.service.LLMService;
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
import com.gac.lms.module.evaluation.vo.EvaluationActionVO;
import com.gac.lms.module.evaluation.vo.EvaluationResultVO;
import com.gac.lms.module.evaluation.vo.PendingItemVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * EvaluationServiceImpl 单元测试。
 *
 * <p>覆盖：客观题判分 4 种题型 / AI 评阅（Mock）/ 人工评阅 / 复核 / 发布 / 待评列表。</p>
 *
 * @author 方雨菲
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class EvaluationServiceImplTest {

    @Mock private GradeRecordMapper gradeRecordMapper;
    @Mock private GradeDetailMapper gradeDetailMapper;
    @Mock private LLMService llmService;

    private EvaluationServiceImpl service;

    private static final Long EXAM_ID = 1001L;
    private static final Long USER_ID = 8888L;

    @BeforeEach
    void setUp() {
        service = new EvaluationServiceImpl(gradeRecordMapper, gradeDetailMapper, llmService);
    }

    // ========== autoEvaluate - 客观题判分 ==========

    @Test
    @DisplayName("autoEvaluate - 单选答对：满分")
    void autoEvaluate_singleCorrect_fullScore() {
        when(gradeRecordMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);

        AutoEvaluateRequest req = new AutoEvaluateRequest();
        req.setExamId(EXAM_ID);
        req.setUserId(USER_ID);
        AutoEvaluateRequest.QuestionAnswer qa = new AutoEvaluateRequest.QuestionAnswer();
        qa.setQuestionId(1L);
        qa.setType("SINGLE");
        qa.setUserAnswer("A");
        qa.setCorrectAnswer("A");
        qa.setFullScore(20);
        req.setAnswers(Arrays.asList(qa));

        EvaluationResultVO result = service.autoEvaluate(req);

        assertThat(result.getTotalScore()).isEqualByComparingTo(new BigDecimal("20"));
        assertThat(result.getObjectiveScore()).isEqualByComparingTo(new BigDecimal("20"));
        assertThat(result.getStatus()).isEqualTo(EvaluationServiceImpl.STATUS_GRADED);
        assertThat(result.getIsPassed()).isEqualTo(0); // 20 < 60 不通过
        assertThat(result.getDetails()).hasSize(1);
        assertThat(result.getDetails().get(0).getIsCorrect()).isEqualTo(1);
    }

    @Test
    @DisplayName("autoEvaluate - 单选答错：0 分")
    void autoEvaluate_singleWrong_zeroScore() {
        when(gradeRecordMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);

        AutoEvaluateRequest req = buildReq(
                qa("SINGLE", "B", "A", 20)
        );

        EvaluationResultVO result = service.autoEvaluate(req);

        assertThat(result.getTotalScore()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getIsPassed()).isEqualTo(0);
    }

    @Test
    @DisplayName("autoEvaluate - 多选全对：满分（乱序）")
    void autoEvaluate_multiCorrect_unordered() {
        when(gradeRecordMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);

        AutoEvaluateRequest req = buildReq(
                qa("MULTI", "B,A,D", "A,B,D", 20)
        );

        EvaluationResultVO result = service.autoEvaluate(req);

        assertThat(result.getTotalScore()).isEqualByComparingTo(new BigDecimal("20"));
        assertThat(result.getDetails().get(0).getIsCorrect()).isEqualTo(1);
    }

    @Test
    @DisplayName("autoEvaluate - 多选少选：0 分")
    void autoEvaluate_multiPartial_zeroScore() {
        when(gradeRecordMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);

        AutoEvaluateRequest req = buildReq(
                qa("MULTI", "A,B", "A,B,C", 20)
        );

        EvaluationResultVO result = service.autoEvaluate(req);

        assertThat(result.getTotalScore()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getDetails().get(0).getIsCorrect()).isEqualTo(0);
    }

    @Test
    @DisplayName("autoEvaluate - 判断对错：等价匹配")
    void autoEvaluate_judge_correct() {
        when(gradeRecordMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);

        AutoEvaluateRequest req1 = buildReq(qa("JUDGE", "T", "TRUE", 10));
        AutoEvaluateRequest req2 = buildReq(qa("JUDGE", "正确", "TRUE", 10));

        EvaluationResultVO r1 = service.autoEvaluate(req1);
        EvaluationResultVO r2 = service.autoEvaluate(req2);

        assertThat(r1.getTotalScore()).isEqualByComparingTo(new BigDecimal("10"));
        assertThat(r2.getTotalScore()).isEqualByComparingTo(new BigDecimal("10"));
    }

    @Test
    @DisplayName("autoEvaluate - 填空忽略大小写和空格")
    void autoEvaluate_fill_ignoreCaseAndSpace() {
        when(gradeRecordMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);

        AutoEvaluateRequest req = buildReq(qa("FILL", "  Java Virtual Machine  ", "java virtual machine", 10));

        EvaluationResultVO result = service.autoEvaluate(req);

        assertThat(result.getTotalScore()).isEqualByComparingTo(new BigDecimal("10"));
    }

    @Test
    @DisplayName("autoEvaluate - 填空多个可接受答案（|分隔）")
    void autoEvaluate_fill_multipleAccepts() {
        when(gradeRecordMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);

        AutoEvaluateRequest req = buildReq(qa("FILL", "JVM", "Java Virtual Machine|JVM|java vm", 10));

        EvaluationResultVO result = service.autoEvaluate(req);

        assertThat(result.getTotalScore()).isEqualByComparingTo(new BigDecimal("10"));
    }

    @Test
    @DisplayName("autoEvaluate - 包含主观题：状态=部分评分")
    void autoEvaluate_withEssay_pendingManual() {
        when(gradeRecordMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);

        AutoEvaluateRequest req = buildReq(
                qa("SINGLE", "A", "A", 20),
                qa("ESSAY", "我的答案是...", "参考答案...", 20)
        );

        EvaluationResultVO result = service.autoEvaluate(req);

        assertThat(result.getStatus()).isEqualTo(EvaluationServiceImpl.STATUS_PARTIAL);
        assertThat(result.getPendingManualCount()).isEqualTo(1);
        assertThat(result.getIsPassed()).isNull(); // 部分评分时不更新
        assertThat(result.getTotalScore()).isEqualByComparingTo(new BigDecimal("20"));
    }

    @Test
    @DisplayName("autoEvaluate - 全部客观题且及格：状态=已评 + isPassed=1")
    void autoEvaluate_allObjective_passed() {
        when(gradeRecordMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);

        AutoEvaluateRequest req = buildReq(
                qa("SINGLE", "A", "A", 40),
                qa("SINGLE", "B", "B", 30)
        );

        EvaluationResultVO result = service.autoEvaluate(req);

        assertThat(result.getTotalScore()).isEqualByComparingTo(new BigDecimal("70"));
        assertThat(result.getStatus()).isEqualTo(EvaluationServiceImpl.STATUS_GRADED);
        assertThat(result.getIsPassed()).isEqualTo(1);
    }

    // ========== aiEvaluate ==========

    @Test
    @DisplayName("aiEvaluate - Mock AI 返回合法 JSON：正确解析分数")
    void aiEvaluate_mockAiJson_parsesScore() {
        when(gradeRecordMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);
        when(llmService.invoke(any(String.class))).thenReturn("{\"score\": 15, \"comment\": \"部分要点命中\"}");

        AiEvaluateRequest req = new AiEvaluateRequest();
        req.setExamId(EXAM_ID);
        req.setUserId(USER_ID);
        AiEvaluateRequest.EssayAnswer ea = new AiEvaluateRequest.EssayAnswer();
        ea.setQuestionId(5L);
        ea.setStem("请简述 Spring IoC 生命周期");
        ea.setUserAnswer("我的回答...");
        ea.setReferenceAnswer("标准答案...");
        ea.setFullScore(20);
        req.setEssays(Arrays.asList(ea));

        EvaluationResultVO result = service.aiEvaluate(req);

        assertThat(result.getSubjectiveScore()).isEqualByComparingTo(new BigDecimal("15"));
        assertThat(result.getStatus()).isEqualTo(EvaluationServiceImpl.STATUS_GRADED);
        assertThat(result.getIsPassed()).isEqualTo(0); // 15 < 60
        assertThat(result.getDetails()).hasSize(1);
        assertThat(result.getDetails().get(0).getEvaluatorType()).isEqualTo(EvaluationServiceImpl.EVAL_AI);
    }

    @Test
    @DisplayName("aiEvaluate - Mock AI 返回空：降级到 0 分")
    void aiEvaluate_emptyResponse_zeroScore() {
        when(gradeRecordMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);
        when(llmService.invoke(any(String.class))).thenReturn("");

        AiEvaluateRequest req = new AiEvaluateRequest();
        req.setExamId(EXAM_ID);
        req.setUserId(USER_ID);
        AiEvaluateRequest.EssayAnswer ea = new AiEvaluateRequest.EssayAnswer();
        ea.setQuestionId(5L);
        ea.setStem("题目");
        ea.setUserAnswer("答案");
        ea.setFullScore(20);
        req.setEssays(Arrays.asList(ea));

        EvaluationResultVO result = service.aiEvaluate(req);

        assertThat(result.getSubjectiveScore()).isEqualByComparingTo(BigDecimal.ZERO);
        verify(gradeDetailMapper, times(1)).insert(any(GradeDetail.class));
    }

    @Test
    @DisplayName("aiEvaluate - AI 抛业务异常：直接抛 EVALUATION_AI_FAIL")
    void aiEvaluate_businessException_propagates() {
        when(gradeRecordMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);
        when(llmService.invoke(any(String.class)))
                .thenThrow(new BusinessException(ErrorCode.AI_INVOKE_FAIL));

        AiEvaluateRequest req = new AiEvaluateRequest();
        req.setExamId(EXAM_ID);
        req.setUserId(USER_ID);
        AiEvaluateRequest.EssayAnswer ea = new AiEvaluateRequest.EssayAnswer();
        ea.setQuestionId(5L);
        ea.setStem("题");
        ea.setUserAnswer("答");
        ea.setFullScore(20);
        req.setEssays(Arrays.asList(ea));

        assertThatThrownBy(() -> service.aiEvaluate(req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("AI 评卷失败");
    }

    // ========== manualEvaluate ==========

    @Test
    @DisplayName("manualEvaluate - 修改分数：总分 delta 正确")
    void manualEvaluate_modifyScore_totalDelta() {
        GradeRecord record = new GradeRecord();
        record.setId(100L);
        record.setExamId(EXAM_ID);
        record.setUserId(USER_ID);
        record.setStatus(EvaluationServiceImpl.STATUS_GRADED);
        record.setTotalScore(new BigDecimal("60"));
        record.setPassScore(new BigDecimal("60"));
        when(gradeRecordMapper.selectById(100L)).thenReturn(record);

        GradeDetail existing = new GradeDetail();
        existing.setId(1000L);
        existing.setGradeId(100L);
        existing.setQuestionId(5L);
        existing.setScore(new BigDecimal("10"));
        when(gradeDetailMapper.selectOne(any(QueryWrapper.class))).thenReturn(existing);

        ManualEvaluateRequest req = new ManualEvaluateRequest();
        req.setGradeId(100L);
        ManualEvaluateRequest.ManualScoreItem item = new ManualEvaluateRequest.ManualScoreItem();
        item.setQuestionId(5L);
        item.setScore(15);
        item.setIsCorrect(1);
        item.setComment("调整");
        req.setItems(Arrays.asList(item));

        EvaluationResultVO result = service.manualEvaluate(req, 999L);

        assertThat(result.getTotalScore()).isEqualByComparingTo(new BigDecimal("65"));
        verify(gradeDetailMapper).updateById(any(GradeDetail.class));
        verify(gradeDetailMapper, never()).insert(any(GradeDetail.class));
    }

    @Test
    @DisplayName("manualEvaluate - 已发布成绩不可改：抛 EVALUATION_PUBLISHED")
    void manualEvaluate_publishedGrade_throws() {
        GradeRecord record = new GradeRecord();
        record.setId(100L);
        record.setStatus(EvaluationServiceImpl.STATUS_PUBLISHED);
        when(gradeRecordMapper.selectById(100L)).thenReturn(record);

        ManualEvaluateRequest req = new ManualEvaluateRequest();
        req.setGradeId(100L);
        req.setItems(Arrays.asList());

        assertThatThrownBy(() -> service.manualEvaluate(req, 999L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("已发布");
    }

    @Test
    @DisplayName("manualEvaluate - 成绩记录不存在：抛 DATA_NOT_FOUND")
    void manualEvaluate_notFound_throws() {
        when(gradeRecordMapper.selectById(100L)).thenReturn(null);

        ManualEvaluateRequest req = new ManualEvaluateRequest();
        req.setGradeId(100L);
        req.setItems(Arrays.asList());

        assertThatThrownBy(() -> service.manualEvaluate(req, 999L))
                .isInstanceOf(BusinessException.class);
    }

    // ========== review / publish ==========

    @Test
    @DisplayName("review - 状态 = 已评 → 复核成功 → 状态 = 已复核")
    void review_gradedGrade_success() {
        GradeRecord record = new GradeRecord();
        record.setId(100L);
        record.setStatus(EvaluationServiceImpl.STATUS_GRADED);
        when(gradeRecordMapper.selectById(100L)).thenReturn(record);

        EvaluationActionVO vo = service.review(100L, 999L);

        assertThat(vo.getStatus()).isEqualTo(EvaluationServiceImpl.STATUS_REVIEWED);
        assertThat(vo.getMessage()).contains("复核完成");
        ArgumentCaptor<GradeRecord> captor = ArgumentCaptor.forClass(GradeRecord.class);
        verify(gradeRecordMapper).updateById(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(EvaluationServiceImpl.STATUS_REVIEWED);
    }

    @Test
    @DisplayName("review - 状态非已评：抛 OPERATION_NOT_ALLOWED")
    void review_wrongStatus_throws() {
        GradeRecord record = new GradeRecord();
        record.setId(100L);
        record.setStatus(EvaluationServiceImpl.STATUS_PARTIAL);
        when(gradeRecordMapper.selectById(100L)).thenReturn(record);

        assertThatThrownBy(() -> service.review(100L, 999L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("publish - 状态 = 已复核 → 发布成功 → 设置 publishedAt")
    void publish_reviewedGrade_success() {
        GradeRecord record = new GradeRecord();
        record.setId(100L);
        record.setStatus(EvaluationServiceImpl.STATUS_REVIEWED);
        when(gradeRecordMapper.selectById(100L)).thenReturn(record);

        EvaluationActionVO vo = service.publish(100L, 999L);

        assertThat(vo.getStatus()).isEqualTo(EvaluationServiceImpl.STATUS_PUBLISHED);
        ArgumentCaptor<GradeRecord> captor = ArgumentCaptor.forClass(GradeRecord.class);
        verify(gradeRecordMapper).updateById(captor.capture());
        assertThat(captor.getValue().getPublishedAt()).isNotNull();
    }

    @Test
    @DisplayName("publish - 状态 = 已评：跳过复核直接发布（业务允许）")
    void publish_gradedGrade_directPublish() {
        GradeRecord record = new GradeRecord();
        record.setId(100L);
        record.setStatus(EvaluationServiceImpl.STATUS_GRADED);
        when(gradeRecordMapper.selectById(100L)).thenReturn(record);

        EvaluationActionVO vo = service.publish(100L, 999L);

        assertThat(vo.getStatus()).isEqualTo(EvaluationServiceImpl.STATUS_PUBLISHED);
    }

    @Test
    @DisplayName("publish - 状态 = 待评分：抛 OPERATION_NOT_ALLOWED")
    void publish_pendingGrade_throws() {
        GradeRecord record = new GradeRecord();
        record.setId(100L);
        record.setStatus(EvaluationServiceImpl.STATUS_PENDING);
        when(gradeRecordMapper.selectById(100L)).thenReturn(record);

        assertThatThrownBy(() -> service.publish(100L, 999L))
                .isInstanceOf(BusinessException.class);
    }

    // ========== listPending ==========

    @Test
    @DisplayName("listPending - 无筛选：返回所有待评分 + 部分 + 已评")
    void listPending_noFilter_returns() {
        when(gradeRecordMapper.selectCount(any(QueryWrapper.class))).thenReturn(2L);
        GradeRecord r1 = new GradeRecord();
        r1.setId(1L);
        r1.setExamId(1001L);
        r1.setUserId(100L);
        r1.setStatus(EvaluationServiceImpl.STATUS_GRADED);
        GradeRecord r2 = new GradeRecord();
        r2.setId(2L);
        r2.setExamId(1001L);
        r2.setUserId(200L);
        r2.setStatus(EvaluationServiceImpl.STATUS_PARTIAL);
        when(gradeRecordMapper.selectList(any(QueryWrapper.class))).thenReturn(Arrays.asList(r1, r2));

        PageResult<PendingItemVO> page = service.listPending(null, null, 1, 20);

        assertThat(page.getTotal()).isEqualTo(2L);
        assertThat(page.getRecords()).hasSize(2);
        assertThat(page.getRecords().get(0).getStatusLabel()).matches(".*已评.*");
        assertThat(page.getRecords().get(1).getStatusLabel()).matches(".*部分.*");
    }

    // ========== getDetail ==========

    @Test
    @DisplayName("getDetail - 正常返回成绩与明细")
    void getDetail_success() {
        GradeRecord record = new GradeRecord();
        record.setId(100L);
        record.setExamId(EXAM_ID);
        record.setUserId(USER_ID);
        record.setTotalScore(new BigDecimal("85"));
        record.setObjectiveScore(new BigDecimal("50"));
        record.setSubjectiveScore(new BigDecimal("35"));
        record.setIsPassed(1);
        record.setStatus(EvaluationServiceImpl.STATUS_PUBLISHED);
        when(gradeRecordMapper.selectById(100L)).thenReturn(record);

        GradeDetail d1 = new GradeDetail();
        d1.setQuestionId(1L);
        d1.setIsCorrect(1);
        d1.setScore(new BigDecimal("20"));
        d1.setEvaluatorType(EvaluationServiceImpl.EVAL_AUTO);
        when(gradeDetailMapper.selectList(any(QueryWrapper.class))).thenReturn(Arrays.asList(d1));

        EvaluationResultVO result = service.getDetail(100L);

        assertThat(result.getGradeId()).isEqualTo(100L);
        assertThat(result.getTotalScore()).isEqualByComparingTo(new BigDecimal("85"));
        assertThat(result.getDetails()).hasSize(1);
        assertThat(result.getDetails().get(0).getQuestionId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getDetail - 成绩不存在：抛 DATA_NOT_FOUND")
    void getDetail_notFound_throws() {
        when(gradeRecordMapper.selectById(999L)).thenReturn(null);
        assertThatThrownBy(() -> service.getDetail(999L))
                .isInstanceOf(BusinessException.class);
    }

    // ========== 辅助 ==========

    private AutoEvaluateRequest buildReq(AutoEvaluateRequest.QuestionAnswer... qas) {
        AutoEvaluateRequest req = new AutoEvaluateRequest();
        req.setExamId(EXAM_ID);
        req.setUserId(USER_ID);
        req.setAnswers(Arrays.asList(qas));
        return req;
    }

    private AutoEvaluateRequest.QuestionAnswer qa(String type, String user, String correct, int full) {
        AutoEvaluateRequest.QuestionAnswer qa = new AutoEvaluateRequest.QuestionAnswer();
        qa.setQuestionId(System.nanoTime() & 0xFFFF); // 避免重复
        qa.setType(type);
        qa.setUserAnswer(user);
        qa.setCorrectAnswer(correct);
        qa.setFullScore(full);
        return qa;
    }
}
