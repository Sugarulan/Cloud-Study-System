package com.gac.lms.module.paper.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 试卷-题目关联实体（对应表 paper_question）。
 *
 * <p>对应 schema.sql §四.3.3.4 试卷-题目表。
 * <ul>
 *   <li>section —— 大题标题，如"一、单项选择题"</li>
 *   <li>score   —— 该题在试卷中的得分（可与题目 default_score 不同）</li>
 *   <li>sort    —— 组卷排序</li>
 * </ul>
 * </p>
 *
 * @author 王茗瑾
 */
@Data
@TableName("paper_question")
public class PaperQuestion implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 试卷 ID */
    private Long paperId;

    /** 题目 ID */
    private Long questionId;

    /** 大题标题：如"一、单项选择题" */
    private String section;

    /** 该题得分 */
    private BigDecimal score;

    /** 排序 */
    private Integer sort;

    /** 创建人 */
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
