package com.gac.lms.module.exam.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 答卷快照（断点续答用）。
 *
 * @author 方雨菲
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "答卷快照")
public class TakingSnapshotVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 考试 ID */
    private Long examId;

    /** 试卷 ID */
    private Long paperId;

    /** 当前快照版本号 */
    private Integer version;

    /** 开始作答时间 */
    private LocalDateTime startTime;

    /** 题目 ID -> 答案 */
    private Map<Long, String> answers;

    /** 已答题数 */
    private Integer answeredCount;

    /** 总题数 */
    private Integer totalCount;
}
