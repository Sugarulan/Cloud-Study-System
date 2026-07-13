package com.gac.lms.module.integration.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 邮件发送日志。
 *
 * <p>W2 阶段：不真正发送，仅记录调用日志（Mock Provider）。</p>
 * <p>W4 阶段：替换为真实 SMTP 发送。</p>
 *
 * @author 方雨菲
 */
@Data
@TableName("sys_email_log")
public class EmailLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 模板代码 */
    private String templateCode;

    /** 收件人邮箱 */
    private String toEmail;

    /** 主题 */
    private String subject;

    /** 内容快照 */
    private String content;

    /** 状态：0=待发 1=成功 2=失败 */
    private Integer status;

    /** 错误信息 */
    private String errorMessage;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
