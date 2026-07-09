package com.gac.lms.module.exam.entity;

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
 * 考试作答记录实体。
 *
 * <p>对应表 {@code exam_taking}。
 * Redis 中的答题快照（{@code exam:taking:v2:{examId}:{userId}}）用于
 * 实时暂存与断点续答，最终交卷后异步（或同步）落库到本表。</p>
 *
 * @author 方雨菲
 */
@Data
@TableName("exam_taking")
public class ExamTaking implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 考试 ID */
    private Long examId;

    /** 学员 ID */
    private Long userId;

    /** 试卷 ID */
    private Long paperId;

    /** 状态：0=进行中 1=已交卷 2=已评分 3=已发布 */
    private Integer status;

    /** 开始作答时间 */
    private LocalDateTime startTime;

    /** 交卷时间 */
    private LocalDateTime submitTime;

    /** 实际用时（秒） */
    private Integer durationSec;

    /** 答题快照版本号（乐观锁） */
    private Integer snapshotVersion;

    /** 答案 JSON（最终落库） */
    private String answersJson;

    // ===== 公共字段 =====

    @TableField(fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT)
    private Long createBy;

    @TableField(fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT_UPDATE)
    private Long updateBy;

    @TableField(fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 逻辑删除标记 */
    @TableLogic
    private Integer deleted;

    /** 乐观锁版本号（MyBatis-Plus @Version） */
    @Version
    private Integer version;
}
