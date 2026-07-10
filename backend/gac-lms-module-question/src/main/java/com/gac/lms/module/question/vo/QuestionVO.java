package com.gac.lms.module.question.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 题目出参 VO（含选项与标签）。
 *
 * <p>answer 字段用 {@link JsonNode} 返回，前端按 type 解释：
 * <ul>
 *   <li>SINGLE → 字符串（"C"）</li>
 *   <li>MULTI  → 字符串数组（["A","B"]）</li>
 *   <li>JUDGE  → 布尔（true/false）</li>
 *   <li>ESSAY  → 字符串</li>
 *   <li>FILL   → 字符串数组</li>
 * </ul>
 * </p>
 *
 * @author 王茗瑾
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "题目详情")
public class QuestionVO {

    @Schema(description = "题目 ID")
    private Long id;

    @Schema(description = "题型")
    private String type;

    @Schema(description = "题干")
    private String stem;

    @Schema(description = "解析")
    private String analysis;

    @Schema(description = "难度：1-5")
    private Integer difficulty;

    @Schema(description = "默认分值")
    private BigDecimal defaultScore;

    @Schema(description = "分类 ID")
    private Long categoryId;

    @Schema(description = "状态：0=草稿 1=已发布")
    private Integer status;

    @Schema(description = "答案（按 type 解释）")
    private JsonNode answer;

    @Schema(description = "选项列表")
    private List<OptionVO> options;

    @Schema(description = "标签 ID 列表")
    private List<Long> tagIds;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Data
    @Builder
    public static class OptionVO {
        @Schema(description = "选项 ID")
        private Long id;

        @Schema(description = "选项键 A/B/C/D")
        private String optKey;

        @Schema(description = "选项内容")
        private String optValue;

        @Schema(description = "是否正确答案（管理端可见，学员端不带）")
        private Boolean isCorrect;

        @Schema(description = "排序")
        private Integer sort;
    }
}
