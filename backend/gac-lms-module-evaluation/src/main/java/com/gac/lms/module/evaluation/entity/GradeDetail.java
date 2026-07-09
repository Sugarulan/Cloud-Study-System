package com.gac.lms.module.evaluation.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 成绩明细实体。
 *
 * <p>对应表 {@code grade_detail}。每条成绩记录对应多条明细（每题一条）。</p>
 *
 * <p>评卷方式 {@code evaluator_type}：</p>
 * <ul>
 *   <li>0 = 自动评阅（客观题）</li>
 *   <li>1 = AI 评阅（主观题）</li>
 *   <li>2 = 人工评阅（主观题）</li>
 * </ul>
 *
 * @author 方雨菲
 */
@Data
@TableName("grade_detail")
public class GradeDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 成绩 ID */
    private Long gradeId;

    /** 题目 ID */
    private Long questionId;

    /** 学员答案 */
    private String userAnswer;

    /** 正确答案 */
    private String correctAnswer;

    /** 是否正确：0=否 1=是 */
    private Integer isCorrect;

    /** 得分 */
    private BigDecimal score;

    /** 满分 */
    private BigDecimal fullScore;

    /** 评卷方式：0=自动 1=AI 2=人工 */
    private Integer evaluatorType;

    /** 人工评卷人 ID（仅人工评卷时使用） */
    private Long evaluatorId;

    /** 评语 */
    private String comment;

    // ===== 公共字段 =====

    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;

    @Version
    private Integer version;
}
