package com.gac.lms.module.grade.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 成绩统计 VO。
 *
 * <p>包含 4 个核心指标 + 分数段分布。</p>
 *
 * @author 方雨菲
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "成绩统计")
public class GradeStatisticsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 考试 ID */
    private Long examId;

    /** 参考人数 */
    private Long totalCount;

    /** 已交卷人数（status >= 1） */
    private Long submittedCount;

    /** 已评分人数（status >= 2） */
    private Long gradedCount;

    /** 已发布人数（status = 4） */
    private Long publishedCount;

    /** 平均分 */
    private BigDecimal averageScore;

    /** 最高分 */
    private BigDecimal maxScore;

    /** 最低分 */
    private BigDecimal minScore;

    /** 通过人数（isPassed = 1） */
    private Long passedCount;

    /** 通过率（0-1，例如 0.85 表示 85%） */
    private BigDecimal passRate;

    /** 分数段分布：[{range: "0-59", count: 10}, ...] */
    private List<ScoreBucket> distribution;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "分数段")
    public static class ScoreBucket implements Serializable {
        private static final long serialVersionUID = 1L;

        /** 分数段名称，例如 "0-59" */
        private String range;

        /** 该段人数 */
        private Long count;

        /** 该段占比（0-1） */
        private BigDecimal ratio;
    }
}
