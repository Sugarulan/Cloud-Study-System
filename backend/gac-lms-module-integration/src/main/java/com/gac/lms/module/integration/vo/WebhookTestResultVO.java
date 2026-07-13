package com.gac.lms.module.integration.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Webhook 测试推送结果。
 *
 * @author 方雨菲
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookTestResultVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long webhookId;
    private String url;
    private Integer httpStatus;
    private String responseBody;
    private Boolean success;
    private String errorMessage;
    private Long costMs;
}
