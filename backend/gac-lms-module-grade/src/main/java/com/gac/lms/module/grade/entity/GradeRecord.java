package com.gac.lms.module.grade.entity;

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
 * 成绩主表实体（成绩管理模块视图）。
 *
 * <p>与 {@code gac-lms-module-evaluation} 模块共享同一张表 {@code grade_record}，
 * 本模块以"查询 / 统计 / 导出"为主，不参与写流程。</p>
 *
 * @author 方雨菲
 */
@Data
@TableName("grade_record")
public class GradeRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long examId;
    private Long userId;
    private Long paperId;
    private BigDecimal totalScore;
    private BigDecimal objectiveScore;
    private BigDecimal subjectiveScore;
    private BigDecimal passScore;
    private Integer isPassed;
    private Integer status;
    private LocalDateTime submittedAt;
    private LocalDateTime publishedAt;

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
