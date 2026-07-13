package com.gac.lms.module.question.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 题目选项实体（对应表 question_option）。
 *
 * <p>仅单选题/多选题使用。判断题、简答题、填空题无需此表。</p>
 *
 * <p><b>注意：</b>本表 schema 没有 {@code version} 列，所以<b>不继承 BaseEntity</b>，
 * 独立声明字段（与 schema 完全对齐）。</p>
 *
 * @author 王茗瑾
 */
@Data
@TableName("question_option")
public class QuestionOption implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 题目 ID */
    private Long questionId;

    /** 选项键：A / B / C / D */
    private String optKey;

    /** 选项内容 */
    private String optValue;

    /** 是否为正确答案 */
    private Boolean isCorrect;

    /** 排序 */
    private Integer sort;

    /** 创建人 */
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新人 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 逻辑删除标记 */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}
