package com.gac.lms.module.exam.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 交卷结果。
 *
 * @author 方雨菲
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "交卷结果")
public class SubmitResultVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 作答记录 ID */
    private Long takingId;

    /** 考试 ID */
    private Long examId;

    /** 提交时间 */
    private LocalDateTime submitTime;

    /** 实际用时（秒） */
    private Integer durationSec;

    /** 答题数 */
    private Integer answeredCount;

    /** 提示信息 */
    private String message;
}
