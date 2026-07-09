package com.gac.lms.module.evaluation.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评卷动作结果（人工/复核/发布通用）。
 *
 * @author 方雨菲
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "评卷动作结果")
public class EvaluationActionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 成绩 ID */
    private Long gradeId;

    /** 当前状态 */
    private Integer status;

    /** 当前状态描述 */
    private String statusLabel;

    /** 操作时间 */
    private LocalDateTime actionTime;

    /** 操作人 */
    private Long operatorId;

    /** 提示信息 */
    private String message;
}
