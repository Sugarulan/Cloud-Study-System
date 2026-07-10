package com.gac.lms.module.selfTest.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 错题本实体。
 *
 * <p>对应表 {@code wrong_question}。从 {@code grade_detail} 中 isCorrect=0 的记录自动生成。</p>
 *
 * @author 方雨菲
 */
@Data
@TableName("wrong_question")
public class WrongQuestion implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 学员 ID */
    private Long userId;

    /** 成绩 ID */
    private Long gradeId;

    /** 考试 ID（冗余字段，便于查询） */
    private Long examId;

    /** 题目 ID */
    private Long questionId;

    /** 学员答案 */
    private String userAnswer;

    /** 正确答案 */
    private String correctAnswer;

    /** 是否已掌握：0=否 1=是 */
    private Integer isMastered;

    /** AI 解析 */
    private String aiExplanation;

    /** AI 解析生成时间 */
    private LocalDateTime aiExplainedAt;

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
