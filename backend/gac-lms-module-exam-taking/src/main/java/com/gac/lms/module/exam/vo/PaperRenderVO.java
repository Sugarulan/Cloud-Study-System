package com.gac.lms.module.exam.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 试卷渲染数据（学员进入考试时拉取）。
 *
 * <p>注意：客观题不返回正确答案，主观题不返回参考答案（防作弊）。
 * 答案仅在交卷后由评卷模块读取。</p>
 *
 * @author 方雨菲
 */
@Data
@Schema(description = "试卷渲染数据")
public class PaperRenderVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 考试 ID */
    private Long examId;

    /** 试卷 ID */
    private Long paperId;

    /** 试卷标题 */
    private String title;

    /** 考试开始时间 */
    private LocalDateTime startTime;

    /** 考试结束时间 */
    private LocalDateTime endTime;

    /** 时长（分钟） */
    private Integer durationMinutes;

    /** 总分 */
    private Integer totalScore;

    /** 题目列表（不含答案） */
    private List<QuestionVO> questions;

    /** 当前快照版本号（首次进入为 0） */
    private Integer currentVersion;

    /**
     * 题目 VO（不含正确答案）。
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "题目")
    public static class QuestionVO implements Serializable {
        private static final long serialVersionUID = 1L;
        private Long id;
        /** SINGLE / MULTI / JUDGE / FILL / ESSAY */
        private String type;
        private String stem;
        /** 选择题选项（key=A/B/C/D） */
        private List<Option> options;
        /** 该题分数 */
        private Integer score;
        /** 难度 */
        private String difficulty;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "选项")
    public static class Option implements Serializable {
        private static final long serialVersionUID = 1L;
        private String key;
        private String value;
    }
}
