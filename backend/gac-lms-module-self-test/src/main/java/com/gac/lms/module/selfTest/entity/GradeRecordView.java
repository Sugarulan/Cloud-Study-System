package com.gac.lms.module.selfTest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 成绩视图（个人测评模块只读视图）。
 *
 * <p>与 {@code gac-lms-module-grade} 共享同一张表 {@code grade_record}。</p>
 *
 * @author 方雨菲
 */
@Data
@TableName("grade_record")
public class GradeRecordView implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long examId;
    private Long userId;
    private Long paperId;
    private BigDecimal totalScore;
    private BigDecimal passScore;
    private Integer isPassed;
    private Integer status;
    private LocalDateTime submittedAt;
    private LocalDateTime publishedAt;

    @TableField(select = false)
    private Integer deleted;

    @Version
    private Integer version;
}
