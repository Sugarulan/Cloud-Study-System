package com.gac.lms.module.account.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gac.lms.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色实体（对应表 role）。
 *
 * <p>对应 schema.sql §一.3.3.1 角色表。
 * 预置角色编码：ADMIN / TEACHER / STUDENT。</p>
 *
 * @author 王茗瑾
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("role")
public class Role extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 角色编码（唯一）：ADMIN / TEACHER / STUDENT */
    private String code;

    /** 角色名称 */
    private String name;

    /** 描述 */
    private String description;

    /** 排序 */
    private Integer sort;

    /** 状态：0=禁用，1=启用 */
    private Integer status;
}
