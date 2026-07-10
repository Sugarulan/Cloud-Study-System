package com.gac.lms.module.knowledge.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 文档版本对比结果。
 *
 * <p>W2 实现：返回两个版本的内容字符串，前端自行 diff 渲染。
 * W3 增强：返回结构化的 diff（新增/删除/修改行）。</p>
 *
 * @author 方雨菲
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocDiffVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long docId;
    private Integer fromVersion;
    private Integer toVersion;
    private String fromContent;
    private String toContent;
    private String unifiedDiff;

    /** 简单字数差异（参考） */
    private Integer addedChars;
    private Integer removedChars;
}
