package com.gac.lms.module.selfTest.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 考试列表项（待考 / 已考共用）。
 *
 * @author 方雨菲
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "考试列表项")
public class ExamItemVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 考试 ID */
    private Long examId;

    /** 考试名称（W3 接入考试服务后填充） */
    private String examName;

    /** 试卷 ID */
    private Long paperId;

    /** 开始时间 */
    private LocalDateTime startTime;

    /** 截止时间 */
    private LocalDateTime endTime;

    /** 交卷时间（已考列表专用） */
    private LocalDateTime submittedAt;

    /** 时长（分钟） */
    private Integer durationMinutes;

    /** 总分 */
    private BigDecimal totalScore;

    /** 状态描述：待考 / 进行中 / 已交卷 / 已发布 */
    private String statusLabel;

    // 已考列表专用字段

    /** 成绩 ID（已考列表） */
    private Long gradeId;

    /** 学员得分（已考列表） */
    private BigDecimal userScore;

    /** 是否通过（已考列表） */
    private Integer isPassed;

    /** 错题数（已考列表） */
    private Integer wrongCount;
}
