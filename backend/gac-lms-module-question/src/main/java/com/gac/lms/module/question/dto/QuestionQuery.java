package com.gac.lms.module.question.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 题目分页查询条件。
 *
 * @author 王茗瑾
 */
@Data
@Schema(description = "题目分页查询条件")
public class QuestionQuery {

    @Schema(description = "题型：SINGLE/MULTI/JUDGE/ESSAY/FILL", example = "SINGLE")
    private String type;

    @Schema(description = "难度：1=易 2=较易 3=中 4=较难 5=难", example = "2")
    private Integer difficulty;

    @Schema(description = "标签 ID（按 tag 过滤）")
    private Long tagId;

    @Schema(description = "关键字（模糊匹配 stem）")
    private String keyword;

    @Schema(description = "状态：0=草稿 1=已发布")
    private Integer status;

    @Schema(description = "页码，从 1 开始", example = "1")
    private Long pageNum = 1L;

    @Schema(description = "每页大小", example = "10")
    private Long pageSize = 10L;
}
