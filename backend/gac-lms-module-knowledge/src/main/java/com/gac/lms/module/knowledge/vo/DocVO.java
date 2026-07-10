package com.gac.lms.module.knowledge.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 知识库文档 VO。
 *
 * @author 方雨菲
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "知识库文档")
public class DocVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long categoryId;
    private String categoryName;
    private String title;
    private String summary;
    private String content;
    private String tags;
    private Integer status;
    private String statusLabel;
    private Integer version;
    private Long authorId;
    private String authorName;
    private Long reviewerId;
    private LocalDateTime publishedAt;
    private LocalDateTime archivedAt;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
