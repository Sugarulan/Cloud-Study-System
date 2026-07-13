package com.gac.lms.module.integration.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 邮件发送请求。
 *
 * @author 方雨菲
 */
@Data
@Schema(description = "邮件发送请求")
public class EmailSendRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotEmpty(message = "收件人不能为空")
    @Schema(description = "收件人邮箱列表")
    private List<@Email String> to;

    @NotBlank
    private String subject;

    /** 纯文本 / HTML */
    private String content;

    /** 可选：是否 HTML */
    private Boolean html = false;

    /** 模板代码（使用 sendTemplate 时使用） */
    private String templateCode;

    /** 模板参数 */
    private Map<String, String> params;
}
