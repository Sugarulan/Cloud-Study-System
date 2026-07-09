package com.gac.lms.module.examMgmt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gac.lms.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 考试实体（对应表 exam）。
 *
 * <p>对应 schema.sql §五.3.3.5 考试表。
 * <ul>
 *   <li>状态 status：0=草稿，1=待发布，2=进行中，3=已结束</li>
 *   <li>submission_rule：0=手动交卷，1=到时自动</li>
 *   <li>duration_min —— 单人作答时长（分钟）</li>
 * </ul>
 * </p>
 *
 * @author 王茗瑾
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("exam")
public class Exam extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 考试标题 */
    private String title;

    /** 试卷 ID */
    private Long paperId;

    /** 考试描述 */
    private String description;

    /** 开始时间 */
    private LocalDateTime startTime;

    /** 结束时间 */
    private LocalDateTime endTime;

    /** 单人作答时长（分钟） */
    private Integer durationMin;

    /** 交卷规则：0=手动交卷，1=到时自动 */
    private Integer submissionRule;

    /** 最大参考次数 */
    private Integer maxAttempts;

    /** 状态：0=草稿，1=待发布，2=进行中，3=已结束 */
    private Integer status;

    /** 发布时间 */
    private LocalDateTime publishedAt;
}
