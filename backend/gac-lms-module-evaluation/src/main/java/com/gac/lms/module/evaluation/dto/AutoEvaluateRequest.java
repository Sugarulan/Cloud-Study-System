package com.gac.lms.module.evaluation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 自动评阅请求（含学员答案 + 标准答案）。
 *
 * <p>W2 阶段：调用方（管理员人工触发或定时任务）传入答案和标准答案，由系统比对判分。
 * W3 阶段：改为从答题快照 + 试卷服务自动拉取。</p>
 *
 * @author 方雨菲
 */
@Data
@Schema(description = "自动评阅请求")
public class AutoEvaluateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 考试 ID */
    @NotNull(message = "考试 ID 不能为空")
    private Long examId;

    /** 学员 ID */
    @NotNull(message = "学员 ID 不能为空")
    private Long userId;

    /** 题目答案列表（含标准答案和学员答案） */
    @NotEmpty(message = "题目答案列表不能为空")
    @Valid
    private List<QuestionAnswer> answers;

    @Data
    @Schema(description = "题目答案")
    public static class QuestionAnswer implements Serializable {
        private static final long serialVersionUID = 1L;

        /** 题目 ID */
        @NotNull
        private Long questionId;

        /** 题目类型：SINGLE/MULTI/JUDGE/FILL/ESSAY */
        @NotNull
        private String type;

        /** 学员答案 */
        private String userAnswer;

        /** 标准答案 */
        private String correctAnswer;

        /** 满分 */
        @NotNull
        private Integer fullScore;
    }
}
