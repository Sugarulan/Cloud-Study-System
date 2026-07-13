package com.gac.lms.module.integration.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Webhook 配置。
 *
 * <p>配置外部回调地址，事件触发时推送 POST 请求。</p>
 *
 * @author 方雨菲
 */
@Data
@TableName("sys_webhook_config")
public class WebhookConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** Webhook 名称 */
    private String name;

    /** 回调 URL */
    private String url;

    /** 订阅事件类型（逗号分隔） */
    private String events;

    /** 密钥（用于签名） */
    private String secret;

    /** 状态：0=禁用 1=启用 */
    private Integer status;

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
}
