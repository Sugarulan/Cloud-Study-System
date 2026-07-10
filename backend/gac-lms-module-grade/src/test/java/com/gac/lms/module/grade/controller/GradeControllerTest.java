package com.gac.lms.module.grade.controller;

import com.gac.lms.common.exception.BusinessException;
import com.gac.lms.common.exception.GlobalExceptionHandler;
import com.gac.lms.common.response.ErrorCode;
import com.gac.lms.common.response.PageResult;
import com.gac.lms.module.grade.service.GradeService;
import com.gac.lms.module.grade.vo.GradeRowVO;
import com.gac.lms.module.grade.vo.GradeStatisticsVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * GradeController 单元测试。
 *
 * @author 方雨菲
 */
class GradeControllerTest {

    private MockMvc mockMvc;
    private GradeService gradeService;

    @BeforeEach
    void setUp() {
        gradeService = mock(GradeService.class);
        GradeController controller = new GradeController(gradeService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /health")
    void health_ok() throws Exception {
        mockMvc.perform(get("/api/v1/grades/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value("grade-module-ok"));
    }

    @Test
    @DisplayName("GET / - 默认参数：pageNum=1 pageSize=20")
    void query_defaults() throws Exception {
        PageResult<GradeRowVO> page = new PageResult<>(0L, 1L, 20L, Collections.emptyList());
        when(gradeService.query(any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/grades"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.pageNum").value(1))
                .andExpect(jsonPath("$.data.pageSize").value(20));
    }

    @Test
    @DisplayName("GET /?examId=1001 - 单条件筛选")
    void query_withExamId() throws Exception {
        PageResult<GradeRowVO> page = new PageResult<>(3L, 1L, 20L, Collections.emptyList());
        when(gradeService.query(any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/grades").param("examId", "1001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(3));
    }

    @Test
    @DisplayName("GET /{id} - 详情")
    void detail_success() throws Exception {
        GradeRowVO vo = GradeRowVO.builder().id(100L).totalScore(new BigDecimal("85")).build();
        when(gradeService.getDetail(eq(100L))).thenReturn(vo);

        mockMvc.perform(get("/api/v1/grades/{id}", 100))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(100))
                .andExpect(jsonPath("$.data.totalScore").value(85));
    }

    @Test
    @DisplayName("GET /statistics?examId=1001 - 统计")
    void statistics_success() throws Exception {
        GradeStatisticsVO vo = GradeStatisticsVO.builder()
                .examId(1001L)
                .totalCount(50L)
                .passedCount(40L)
                .passRate(new BigDecimal("0.8"))
                .build();
        when(gradeService.statistics(eq(1001L))).thenReturn(vo);

        mockMvc.perform(get("/api/v1/grades/statistics").param("examId", "1001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.totalCount").value(50))
                .andExpect(jsonPath("$.data.passRate").value(0.8));
    }

    @Test
    @DisplayName("GET /statistics - 缺 examId：业务码 BAD_REQUEST")
    void statistics_missingExamId() throws Exception {
        mockMvc.perform(get("/api/v1/grades/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.BAD_REQUEST.getCode()));
    }

    @Test
    @DisplayName("GET /{id} - 不存在：业务码 DATA_NOT_FOUND")
    void detail_notFound() throws Exception {
        when(gradeService.getDetail(eq(999L)))
                .thenThrow(new BusinessException(ErrorCode.DATA_NOT_FOUND));

        mockMvc.perform(get("/api/v1/grades/{id}", 999))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.DATA_NOT_FOUND.getCode()));
    }
}
