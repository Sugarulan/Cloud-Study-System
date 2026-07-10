package com.gac.lms.module.question.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 更新题目请求体 —— 字段同 {@link QuestionCreateCmd}，但不允许改 type。
 *
 * <p>type 不可变理由：题目类型一旦确定，关联的 answer_json / options 格式就锁定了，
 * 改 type 会让已有数据格式错乱。</p>
 *
 * @author 王茗瑾
 */
@Data
@Schema(description = "更新题目请求体")
public class QuestionUpdateCmd {

    @Schema(description = "题干（支持富文本/Markdown）")
    private String stem;

    @Schema(description = "解析")
    private String analysis;

    @Schema(description = "难度：1=易 2=较易 3=中 4=较难 5=难")
    private Integer difficulty;

    @Schema(description = "默认分值")
    private java.math.BigDecimal defaultScore;

    @Schema(description = "分类 ID（预留）")
    private Long categoryId;

    @Schema(description = "答案（按 type 解释）")
    private Object answer;

    @Schema(description = "选项列表（仅 SINGLE/MULTI 需要）")
    private List<QuestionCreateCmd.QuestionOptionCmd> options;

    @Schema(description = "标签 ID 列表（替换式）")
    private List<Long> tagIds;
}
