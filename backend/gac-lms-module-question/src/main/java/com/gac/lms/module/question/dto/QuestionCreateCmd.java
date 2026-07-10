package com.gac.lms.module.question.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 创建题目请求体。
 *
 * <p>关键字段说明：</p>
 * <ul>
 *   <li>{@code type} 决定 {@code answer} 与 {@code options} 的格式</li>
 *   <li>{@code answer} 用 {@code Object} 接收，由 Service 按 type 校验</li>
 *   <li>{@code options} 仅 SINGLE/MULTI 需要</li>
 *   <li>{@code tagIds} 通过 {@code question_tag} 中间表关联</li>
 * </ul>
 *
 * @author 王茗瑾
 */
@Data
@Schema(description = "创建题目请求体")
public class QuestionCreateCmd {

    @Schema(description = "题型", example = "SINGLE",
            allowableValues = {"SINGLE", "MULTI", "JUDGE", "ESSAY", "FILL"})
    private String type;

    @Schema(description = "题干（支持富文本/Markdown）", example = "Java 中用于保证可见性的关键字是？")
    private String stem;

    @Schema(description = "解析")
    private String analysis;

    @Schema(description = "难度：1=易 2=较易 3=中 4=较难 5=难", example = "2")
    private Integer difficulty;

    @Schema(description = "默认分值", example = "5.00")
    private java.math.BigDecimal defaultScore;

    @Schema(description = "分类 ID（预留）")
    private Long categoryId;

    @Schema(description = "答案（SINGLE=string, MULTI=array, JUDGE=boolean, ESSAY=string, FILL=array）")
    private Object answer;

    @Schema(description = "选项列表（仅 SINGLE/MULTI 需要）")
    private List<QuestionOptionCmd> options;

    @Schema(description = "标签 ID 列表")
    private List<Long> tagIds;

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class QuestionOptionCmd {
        @Schema(description = "选项键 A/B/C/D", example = "A")
        private String optKey;

        @Schema(description = "选项内容", example = "volatile")
        private String optValue;

        @Schema(description = "是否为正确答案", example = "true")
        private Boolean isCorrect;

        @Schema(description = "排序", example = "1")
        private Integer sort;
    }
}
