package com.gac.lms.module.account.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gac.lms.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 账号实体（对应表 account）。
 *
 * <p>对应 schema.sql §一.3.3.1 账号表。</p>
 *
 * @author 王茗瑾
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("account")
public class Account extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 主键（雪花算法） */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 登录名 */
    private String username;

    /** BCrypt 密码哈希（对外不返回） */
    private String passwordHash;

    /** 邮箱 */
    private String email;

    /** 手机号 */
    private String phone;

    /** 状态：0=禁用，1=启用 */
    private Integer status;

    /** 最近登录时间 */
    private LocalDateTime lastLoginAt;
}
