package com.gac.lms.module.knowledge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 创建 / 更新文档请求。
 *
 * @author 方雨菲
 */
@Data
@Schema(description = "创建 / 更新文档")
public class DocCreateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "标题不能为空")
    @Size(max = 256, message = "标题不超过 256 字符")
    private String title;

    private Long categoryId;

    @Size(max = 512)
    private String summary;

    /** 富文本内容 */
    private String content;

    /** 标签，逗号分隔 */
    private String tags;

    /** 仅更新操作需要：变更日志 */
    private String changeLog;
}
