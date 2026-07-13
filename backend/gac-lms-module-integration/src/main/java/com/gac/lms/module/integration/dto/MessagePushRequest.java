package com.gac.lms.module.integration.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 站内信推送请求。
 *
 * @author 方雨菲
 */
@Data
@Schema(description = "站内信推送请求")
public class MessagePushRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull
    private Long userId;

    @NotBlank
    private String type;

    @NotBlank
    private String title;

    private String content;
}
