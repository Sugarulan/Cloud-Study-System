package com.gac.lms.module.question.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gac.lms.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 标签实体（对应表 tag）。
 *
 * <p>对应 schema.sql §三.3.3.3 标签表。
 * category 区分标签分类：题目 / 试卷 / 文档。</p>
 *
 * @author 王茗瑾
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tag")
public class Tag extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 标签名称 */
    private String name;

    /** 标签分类：题目 / 试卷 / 文档 */
    private String category;
}
