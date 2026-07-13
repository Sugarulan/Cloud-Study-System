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
 * 站内信实体。
 *
 * <p>对应表 {@code sys_message}。</p>
 *
 * @author 方雨菲
 */
@Data
@TableName("sys_message")
public class SysMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 接收人 ID */
    private Long userId;

    /** 消息类型：EXAM_REMIND / GRADE_PUBLISH / KNOWLEDGE_PUBLISH 等 */
    private String type;

    /** 标题 */
    private String title;

    /** 内容 */
    private String content;

    /** 是否已读：0=否 1=是 */
    private Integer isRead;

    /** 阅读时间 */
    private LocalDateTime readTime;

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
}
