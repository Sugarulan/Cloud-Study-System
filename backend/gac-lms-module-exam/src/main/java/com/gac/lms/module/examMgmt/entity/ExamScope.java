package com.gac.lms.module.examMgmt.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 考试参考范围实体（对应表 exam_scope）。
 *
 * <p>对应 schema.sql §五.3.3.5 考试-参考范围表。
 * scope_type 决定 target_id 含义：
 * <ul>
 *   <li>1=全员（target_id=0）</li>
 *   <li>2=部门（target_id=dept_id）</li>
 *   <li>3=人员（target_id=person_id）</li>
 *   <li>4=角色（target_id=role_id）</li>
 * </ul>
 * </p>
 *
 * @author 王茗瑾
 */
@Data
@TableName("exam_scope")
public class ExamScope implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 考试 ID */
    private Long examId;

    /** 参考范围类型：1=全员，2=部门，3=人员，4=角色 */
    private Integer scopeType;

    /** 目标 ID（按 scope_type 解释） */
    private Long targetId;

    /** 创建人 */
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
