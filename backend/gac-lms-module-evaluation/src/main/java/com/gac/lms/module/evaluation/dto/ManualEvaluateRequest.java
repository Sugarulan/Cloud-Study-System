package com.gac.lms.module.evaluation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 人工评阅请求。
 *
 * @author 方雨菲
 */
@Data
@Schema(description = "人工评阅请求")
public class ManualEvaluateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 成绩 ID */
    @NotNull
    private Long gradeId;

    /** 评卷人 ID（从 X-User-Id 读取，可不传） */
    private Long evaluatorId;

    /** 每题得分 */
    @NotEmpty
    @Valid
    private List<ManualScoreItem> items;

    @Data
    @Schema(description = "单题人工打分")
    public static class ManualScoreItem implements Serializable {
        private static final long serialVersionUID = 1L;

        @NotNull
        private Long questionId;

        /** 得分 */
        @NotNull
        private Integer score;

        /** 是否正确（人工标记） */
        private Integer isCorrect;

        /** 评语 */
        private String comment;
    }
}
