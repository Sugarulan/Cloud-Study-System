package com.gac.lms.module.question.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gac.lms.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 题目选项实体（对应表 question_option）。
 *
 * <p>仅单选题/多选题使用。判断题、简答题、填空题无需此表。</p>
 *
 * @author 王茗瑾
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("question_option")
public class QuestionOption extends BaseEntity {

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
}
