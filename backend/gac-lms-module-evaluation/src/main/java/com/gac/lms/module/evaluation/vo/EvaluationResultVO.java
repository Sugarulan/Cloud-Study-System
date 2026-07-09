package com.gac.lms.module.evaluation.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 评卷结果 VO。
 *
 * @author 方雨菲
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "评卷结果")
public class EvaluationResultVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 成绩 ID */
    private Long gradeId;

    /** 考试 ID */
    private Long examId;

    /** 学员 ID */
    private Long userId;

    /** 总分 */
    private BigDecimal totalScore;

    /** 客观题得分 */
    private BigDecimal objectiveScore;

    /** 主观题得分（可能为 null，待人工评） */
    private BigDecimal subjectiveScore;

    /** 待人工评题数 */
    private Integer pendingManualCount;

    /** 是否通过（仅在全部评完后才有意义） */
    private Integer isPassed;

    /** 状态：0=待评分 1=部分 2=已评 3=已复核 4=已发布 */
    private Integer status;

    /** 每题得分详情 */
    private List<QuestionResult> details;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "单题结果")
    public static class QuestionResult implements Serializable {
        private static final long serialVersionUID = 1L;
        private Long questionId;
        private Integer isCorrect;
        private BigDecimal score;
        private Integer evaluatorType;
        private String comment;
    }
}
