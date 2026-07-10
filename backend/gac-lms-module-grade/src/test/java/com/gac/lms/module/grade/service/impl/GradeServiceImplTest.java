package com.gac.lms.module.grade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gac.lms.common.exception.BusinessException;
import com.gac.lms.common.response.PageResult;
import com.gac.lms.module.grade.dto.GradeQueryRequest;
import com.gac.lms.module.grade.entity.GradeRecord;
import com.gac.lms.module.grade.mapper.GradeRecordMapper;
import com.gac.lms.module.grade.vo.GradeExportRow;
import com.gac.lms.module.grade.vo.GradeRowVO;
import com.gac.lms.module.grade.vo.GradeStatisticsVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * GradeServiceImpl 单元测试。
 *
 * @author 方雨菲
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class GradeServiceImplTest {

    @Mock private GradeRecordMapper gradeRecordMapper;

    private GradeServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new GradeServiceImpl(gradeRecordMapper);
    }

    // ========== query ==========

    @Test
    @DisplayName("query - 多条件分页：拼接 wrapper 并返回 VO")
    void query_multiConditions_paginate() {
        when(gradeRecordMapper.selectCount(any(QueryWrapper.class))).thenReturn(100L);

        GradeRecord r = new GradeRecord();
        r.setId(1L);
        r.setExamId(1001L);
        r.setUserId(200L);
        r.setTotalScore(new BigDecimal("85"));
        r.setObjectiveScore(new BigDecimal("60"));
        r.setSubjectiveScore(new BigDecimal("25"));
        r.setIsPassed(1);
        r.setStatus(4);
        r.setSubmittedAt(java.time.LocalDateTime.now());
        when(gradeRecordMapper.selectList(any(QueryWrapper.class))).thenReturn(Arrays.asList(r));

        GradeQueryRequest req = new GradeQueryRequest();
        req.setExamId(1001L);
        req.setStatus(4);
        req.setIsPassed(1);
        req.setMinScore(new BigDecimal("60"));
        req.setMaxScore(new BigDecimal("100"));
        req.setPageNum(2L);
        req.setPageSize(10L);

        PageResult<GradeRowVO> page = service.query(req);

        assertThat(page.getTotal()).isEqualTo(100L);
        assertThat(page.getPageNum()).isEqualTo(2L);
        assertThat(page.getPageSize()).isEqualTo(10L);
        assertThat(page.getRecords()).hasSize(1);
        assertThat(page.getRecords().get(0).getTotalScore()).isEqualByComparingTo(new BigDecimal("85"));
        assertThat(page.getRecords().get(0).getStatusLabel()).matches(".*发布.*");
    }

    @Test
    @DisplayName("query - 无任何条件：仅 deleted=0 生效")
    void query_noConditions() {
        when(gradeRecordMapper.selectCount(any(QueryWrapper.class))).thenReturn(0L);
        when(gradeRecordMapper.selectList(any(QueryWrapper.class))).thenReturn(Collections.emptyList());

        GradeQueryRequest req = new GradeQueryRequest();
        PageResult<GradeRowVO> page = service.query(req);

        assertThat(page.getTotal()).isEqualTo(0L);
        assertThat(page.getRecords()).isEmpty();

        // 验证 wrapper 至少包含 deleted=0 条件
        ArgumentCaptor<QueryWrapper<GradeRecord>> captor = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(gradeRecordMapper).selectCount(captor.capture());
        String sql = captor.getValue().getSqlSegment();
        assertThat(sql).contains("deleted");
    }

    @Test
    @DisplayName("query - 时间范围：submittedFrom + submittedTo")
    void query_timeRange() {
        when(gradeRecordMapper.selectCount(any(QueryWrapper.class))).thenReturn(0L);
        when(gradeRecordMapper.selectList(any(QueryWrapper.class))).thenReturn(Collections.emptyList());

        GradeQueryRequest req = new GradeQueryRequest();
        req.setSubmittedFrom(java.time.LocalDateTime.of(2026, 7, 1, 0, 0));
        req.setSubmittedTo(java.time.LocalDateTime.of(2026, 7, 31, 23, 59));
        service.query(req);

        ArgumentCaptor<QueryWrapper<GradeRecord>> captor = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(gradeRecordMapper).selectCount(captor.capture());
        String sql = captor.getValue().getSqlSegment();
        assertThat(sql).contains("submitted_at");
    }

    // ========== getDetail ==========

    @Test
    @DisplayName("getDetail - 存在：返回 VO")
    void getDetail_exists() {
        GradeRecord r = new GradeRecord();
        r.setId(100L);
        r.setExamId(1L);
        r.setUserId(2L);
        r.setStatus(4);
        r.setIsPassed(1);
        r.setTotalScore(new BigDecimal("90"));
        when(gradeRecordMapper.selectById(100L)).thenReturn(r);

        GradeRowVO vo = service.getDetail(100L);

        assertThat(vo.getId()).isEqualTo(100L);
        assertThat(vo.getTotalScore()).isEqualByComparingTo(new BigDecimal("90"));
        assertThat(vo.getIsPassed()).isEqualTo(1);
    }

    @Test
    @DisplayName("getDetail - 不存在：抛 DATA_NOT_FOUND")
    void getDetail_notFound_throws() {
        when(gradeRecordMapper.selectById(999L)).thenReturn(null);
        assertThatThrownBy(() -> service.getDetail(999L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("成绩记录不存在");
    }

    // ========== statistics ==========

    @Test
    @DisplayName("statistics - 考试存在：返回统计 + 分数段分布")
    void statistics_success() {
        GradeStatisticsVO mockVo = GradeStatisticsVO.builder()
                .examId(1001L)
                .totalCount(50L)
                .submittedCount(48L)
                .gradedCount(48L)
                .publishedCount(45L)
                .averageScore(new BigDecimal("72.50"))
                .maxScore(new BigDecimal("98"))
                .minScore(new BigDecimal("32"))
                .passedCount(40L)
                .passRate(new BigDecimal("0.8333"))
                .build();
        when(gradeRecordMapper.statistics(1001L)).thenReturn(mockVo);

        List<GradeStatisticsVO.ScoreBucket> dist = Arrays.asList(
                GradeStatisticsVO.ScoreBucket.builder().range("90-100").count(5L).ratio(new BigDecimal("0.10")).build(),
                GradeStatisticsVO.ScoreBucket.builder().range("80-89").count(15L).ratio(new BigDecimal("0.30")).build(),
                GradeStatisticsVO.ScoreBucket.builder().range("70-79").count(20L).ratio(new BigDecimal("0.40")).build(),
                GradeStatisticsVO.ScoreBucket.builder().range("60-69").count(5L).ratio(new BigDecimal("0.10")).build(),
                GradeStatisticsVO.ScoreBucket.builder().range("0-59").count(5L).ratio(new BigDecimal("0.10")).build()
        );
        when(gradeRecordMapper.distribution(1001L)).thenReturn(dist);

        GradeStatisticsVO result = service.statistics(1001L);

        assertThat(result.getExamId()).isEqualTo(1001L);
        assertThat(result.getTotalCount()).isEqualTo(50L);
        assertThat(result.getPassedCount()).isEqualTo(40L);
        assertThat(result.getPassRate()).isEqualByComparingTo(new BigDecimal("0.8333"));
        assertThat(result.getDistribution()).hasSize(5);
        assertThat(result.getDistribution().get(0).getRange()).isEqualTo("90-100");
    }

    @Test
    @DisplayName("statistics - mapper 返回 null：构造空 VO（避免 NPE）")
    void statistics_mapperReturnsNull_emptyVo() {
        when(gradeRecordMapper.statistics(1001L)).thenReturn(null);
        when(gradeRecordMapper.distribution(1001L)).thenReturn(Collections.emptyList());

        GradeStatisticsVO result = service.statistics(1001L);

        assertThat(result.getExamId()).isEqualTo(1001L);
        assertThat(result.getDistribution()).isEmpty();
    }

    @Test
    @DisplayName("statistics - examId 为 null：抛 BAD_REQUEST")
    void statistics_nullExamId_throws() {
        assertThatThrownBy(() -> service.statistics(null))
                .isInstanceOf(BusinessException.class);
    }

    // ========== exportRows ==========

    @Test
    @DisplayName("exportRows - 返回 VO 列表：包含是否通过与状态中文")
    void exportRows_chineseLabels() {
        GradeRecord r = new GradeRecord();
        r.setId(1L);
        r.setExamId(1001L);
        r.setUserId(200L);
        r.setTotalScore(new BigDecimal("85"));
        r.setIsPassed(1);
        r.setStatus(4);
        when(gradeRecordMapper.selectList(any(QueryWrapper.class))).thenReturn(Arrays.asList(r));

        List<GradeExportRow> rows = service.exportRows(new GradeQueryRequest());

        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).getIsPassedLabel()).isEqualTo("通过");
        assertThat(rows.get(0).getStatusLabel()).matches(".*发布.*");
    }

    @Test
    @DisplayName("exportRows - 未通过：isPassedLabel=未通过")
    void exportRows_failedLabel() {
        GradeRecord r = new GradeRecord();
        r.setId(1L);
        r.setIsPassed(0);
        r.setStatus(2);
        when(gradeRecordMapper.selectList(any(QueryWrapper.class))).thenReturn(Arrays.asList(r));

        List<GradeExportRow> rows = service.exportRows(new GradeQueryRequest());

        assertThat(rows.get(0).getIsPassedLabel()).isEqualTo("未通过");
    }

    @Test
    @DisplayName("exportRows - 上限保护：LIMIT 65535")
    void exportRows_limitCap() {
        when(gradeRecordMapper.selectList(any(QueryWrapper.class))).thenReturn(Collections.emptyList());

        service.exportRows(new GradeQueryRequest());

        ArgumentCaptor<QueryWrapper<GradeRecord>> captor = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(gradeRecordMapper).selectList(captor.capture());
        String sql = captor.getValue().getSqlSegment();
        assertThat(sql).contains("LIMIT 65535");
    }
}
