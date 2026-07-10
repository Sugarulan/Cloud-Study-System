package com.gac.lms.module.selfTest.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 错题列表项 / 错题详情。
 *
 * @author 方雨菲
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "错题")
public class WrongQuestionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 错题 ID */
    private Long id;

    /** 考试 ID */
    private Long examId;

    /** 试卷 ID */
    private Long paperId;

    /** 题目 ID */
    private Long questionId;

    /** 题干 */
    private String stem;

    /** 题目类型 */
    private String questionType;

    /** 选项（JSON 数组字符串） */
    private String optionsJson;

    /** 学员答案 */
    private String userAnswer;

    /** 正确答案 */
    private String correctAnswer;

    /** 是否已掌握 */
    private Integer isMastered;

    /** AI 解析 */
    private String aiExplanation;

    /** AI 解析时间 */
    private LocalDateTime aiExplainedAt;

    /** 关联考试时间（辅助排序） */
    private LocalDateTime submittedAt;
}
