package com.gac.lms.module.paper.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gac.lms.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 试卷实体（对应表 paper）。
 *
 * <p>对应 schema.sql §四.3.3.4 试卷表。
 * 状态 status：0=草稿，1=已发布。</p>
 *
 * @author 王茗瑾
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("paper")
public class Paper extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 试卷标题 */
    private String title;

    /** 试卷描述 */
    private String description;

    /** 总分 */
    private BigDecimal totalScore;

    /** 考试时长（分钟） */
    private Integer durationMin;

    /** 通过分 */
    private BigDecimal passScore;

    /** 题目数量 */
    private Integer questionCount;

    /** 状态：0=草稿，1=已发布 */
    private Integer status;

    /** 发布时间 */
    private LocalDateTime publishedAt;
}
