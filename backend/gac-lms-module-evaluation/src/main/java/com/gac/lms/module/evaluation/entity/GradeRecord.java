package com.gac.lms.module.evaluation.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 成绩主表实体。
 *
 * <p>对应表 {@code grade_record}。一次考试作答记录对应一条成绩记录。</p>
 *
 * <p>状态机：</p>
 * <pre>
 * 0=待评分 → 1=部分评分 → 2=已评分（待复核）→ 3=已复核 → 4=已发布
 * </pre>
 *
 * @author 方雨菲
 */
@Data
@TableName("grade_record")
public class GradeRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 考试 ID */
    private Long examId;

    /** 学员 ID */
    private Long userId;

    /** 试卷 ID */
    private Long paperId;

    /** 总分 */
    private BigDecimal totalScore;

    /** 客观题得分 */
    private BigDecimal objectiveScore;

    /** 主观题得分 */
    private BigDecimal subjectiveScore;

    /** 通过分 */
    private BigDecimal passScore;

    /** 是否通过：0=否 1=是 */
    private Integer isPassed;

    /** 状态：0=待评分 1=部分 2=已评 3=已复核 4=已发布 */
    private Integer status;

    /** 交卷时间 */
    private LocalDateTime submittedAt;

    /** 发布时间 */
    private LocalDateTime publishedAt;

    // ===== 公共字段 =====

    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;

    @Version
    private Integer version;
}
