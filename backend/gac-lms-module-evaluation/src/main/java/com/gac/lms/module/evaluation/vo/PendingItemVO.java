package com.gac.lms.module.evaluation.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 待评卷列表项。
 *
 * @author 方雨菲
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "待评卷列表项")
public class PendingItemVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 成绩 ID */
    private Long gradeId;

    /** 考试 ID */
    private Long examId;

    /** 考试名称 */
    private String examName;

    /** 学员 ID */
    private Long userId;

    /** 学员姓名 */
    private String userName;

    /** 状态：0=待评分 1=部分 2=已评（待复核）3=已复核 4=已发布 */
    private Integer status;

    /** 状态描述 */
    private String statusLabel;

    /** 待人工评题数 */
    private Integer pendingManualCount;

    /** 交卷时间 */
    private LocalDateTime submittedAt;
}
