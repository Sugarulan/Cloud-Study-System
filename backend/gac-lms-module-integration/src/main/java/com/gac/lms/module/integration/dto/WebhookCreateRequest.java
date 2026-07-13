package com.gac.lms.module.integration.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * Webhook 配置请求。
 *
 * @author 方雨菲
 */
@Data
@Schema(description = "Webhook 配置请求")
public class WebhookCreateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank
    private String name;

    @NotBlank
    private String url;

    /** 订阅事件类型，逗号分隔（如 EXAM_PUBLISH,GRADE_PUBLISH） */
    private String events;

    /** 可选签名密钥 */
    private String secret;

    /** 默认启用 1 */
    private Integer status = 1;
}
