package com.gac.lms.module.integration.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Webhook 配置 VO（不含 secret 明文，避免响应泄漏）。
 *
 * @author 方雨菲
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookConfigVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String url;
    private String events;
    /** 仅返回是否配置了 secret，不返回明文 */
    private Boolean hasSecret;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
