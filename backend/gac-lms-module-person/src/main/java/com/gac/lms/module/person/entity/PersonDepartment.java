package com.gac.lms.module.person.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 人员-部门关联实体（对应表 person_department）。
 *
 * <p>对应 schema.sql §二.3.3.2 人员-部门表。
 * 支持一人多部门 + 主部门标记（is_primary=1）。</p>
 *
 * @author 王茗瑾
 */
@Data
@TableName("person_department")
public class PersonDepartment implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 人员 ID */
    private Long personId;

    /** 部门 ID */
    private Long departmentId;

    /** 是否主部门：0=否，1=是 */
    private Integer isPrimary;

    /** 创建人 */
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
