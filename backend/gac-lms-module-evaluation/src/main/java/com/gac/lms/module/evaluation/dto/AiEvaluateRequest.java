package com.gac.lms.module.evaluation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * AI 评阅请求。
 *
 * <p>W2 阶段：调用 AI Provider（默认 Mock）评阅主观题，结果作为待人工复核的参考。
 * W4 阶段：接入企业内网大模型，使用专门设计的 Prompt 模板。</p>
 *
 * @author 方雨菲
 */
@Data
@Schema(description = "AI 评阅请求")
public class AiEvaluateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 考试 ID */
    @NotNull
    private Long examId;

    /** 学员 ID */
    @NotNull
    private Long userId;

    /** 待评主观题列表 */
    @NotEmpty
    private List<EssayAnswer> essays;

    @Data
    @Schema(description = "主观题")
    public static class EssayAnswer implements Serializable {
        private static final long serialVersionUID = 1L;

        @NotNull
        private Long questionId;

        /** 题干（提供给 AI 的上下文） */
        @NotNull
        private String stem;

        /** 参考答案 */
        private String referenceAnswer;

        /** 学员答案 */
        @NotNull
        private String userAnswer;

        /** 满分 */
        @NotNull
        private Integer fullScore;
    }
}
