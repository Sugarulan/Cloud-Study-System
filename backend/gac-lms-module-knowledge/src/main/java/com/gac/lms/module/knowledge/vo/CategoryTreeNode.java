package com.gac.lms.module.knowledge.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 知识库分类树节点。
 *
 * @author 方雨菲
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryTreeNode implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long parentId;
    private String name;
    private Integer sort;

    /** 该分类下的文档数 */
    private Long docCount;

    /** 子分类（递归） */
    private List<CategoryTreeNode> children;
}
