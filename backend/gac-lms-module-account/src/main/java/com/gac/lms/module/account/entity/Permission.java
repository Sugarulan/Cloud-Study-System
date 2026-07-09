package com.gac.lms.module.account.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gac.lms.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 权限实体（对应表 permission）。
 *
 * <p>对应 schema.sql §一.3.3.1 权限表。
 * 权限类型：type=1=菜单，type=2=按钮。</p>
 *
 * @author 王茗瑾
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("permission")
public class Permission extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 权限编码（唯一）：account / account:list 等 */
    private String code;

    /** 权限名称 */
    private String name;

    /** 类型：1=菜单，2=按钮 */
    private Integer type;

    /** 父权限 ID（顶层为 0） */
    private Long parentId;

    /** 排序 */
    private Integer sort;
}
