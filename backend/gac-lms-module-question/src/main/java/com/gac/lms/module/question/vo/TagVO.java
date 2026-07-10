package com.gac.lms.module.question.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 标签出参 VO。
 *
 * @author 王茗瑾
 */
@Data
@Builder
@Schema(description = "标签信息")
public class TagVO {

    @Schema(description = "标签 ID")
    private Long id;

    @Schema(description = "标签名称")
    private String name;

    @Schema(description = "标签分类：题目/试卷/文档")
    private String category;

    @Schema(description = "使用次数（题目数）")
    private Long useCount;
}
