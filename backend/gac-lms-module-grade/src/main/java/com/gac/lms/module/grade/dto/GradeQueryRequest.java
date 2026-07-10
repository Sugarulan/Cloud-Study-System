package com.gac.lms.module.grade.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 成绩多条件筛选请求。
 *
 * <p>所有字段可选，不传则忽略该条件。</p>
 *
 * @author 方雨菲
 */
@Data
@Schema(description = "成绩筛选请求")
public class GradeQueryRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 考试 ID */
    private Long examId;

    /** 学员 ID */
    private Long userId;

    /** 试卷 ID */
    private Long paperId;

    /** 状态：0=待评分 1=部分 2=已评 3=已复核 4=已发布 */
    private Integer status;

    /** 是否通过：0=否 1=是 */
    private Integer isPassed;

    /** 最低分（含） */
    private java.math.BigDecimal minScore;

    /** 最高分（含） */
    private java.math.BigDecimal maxScore;

    /** 提交时间起点 */
    private LocalDateTime submittedFrom;

    /** 提交时间终点 */
    private LocalDateTime submittedTo;

    /** 关键字（搜索用户姓名等，W3 接入账号模块后启用） */
    private String keyword;

    /** 页码（从 1 开始） */
    private Long pageNum = 1L;

    /** 每页大小 */
    private Long pageSize = 20L;
}
