package com.gac.lms.module.grade.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 成绩列表行 VO。
 *
 * @author 方雨菲
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "成绩列表行")
public class GradeRowVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long examId;
    private String examName;
    private Long userId;
    private String userName;
    private Long paperId;
    private BigDecimal totalScore;
    private BigDecimal objectiveScore;
    private BigDecimal subjectiveScore;
    private BigDecimal passScore;
    private Integer isPassed;
    private Integer status;
    private String statusLabel;
    private LocalDateTime submittedAt;
    private LocalDateTime publishedAt;
}
