package com.gac.lms.module.person.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gac.lms.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 部门实体（对应表 department）。
 *
 * <p>对应 schema.sql §二.3.3.2 部门表。树形结构：parent_id=0 表示顶层。</p>
 *
 * @author 王茗瑾
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("department")
public class Department extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 父部门 ID（顶层为 0） */
    private Long parentId;

    /** 部门名称 */
    private String name;

    /** 部门编码 */
    private String code;

    /** 排序 */
    private Integer sort;

    /** 部门负责人 person_id */
    private Long leaderId;

    /** 状态：0=禁用，1=启用 */
    private Integer status;
}
