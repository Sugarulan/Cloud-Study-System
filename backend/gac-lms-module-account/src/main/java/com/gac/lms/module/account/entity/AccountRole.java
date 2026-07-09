package com.gac.lms.module.account.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 账号-角色关联实体（对应表 account_role）。
 *
 * <p>多对多关联表：account_id × role_id。
 * 不继承 BaseEntity——只保留必要的创建审计字段，无 deleted / version。</p>
 *
 * @author 王茗瑾
 */
@Data
@TableName("account_role")
public class AccountRole implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 账号 ID */
    private Long accountId;

    /** 角色 ID */
    private Long roleId;

    /** 创建人 */
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
