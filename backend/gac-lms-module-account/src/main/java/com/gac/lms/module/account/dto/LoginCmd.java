package com.gac.lms.module.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 登录请求体。
 *
 * @author 王茗瑾
 */
@Data
@Schema(description = "登录请求")
public class LoginCmd {

    @Schema(description = "登录名", example = "admin")
    @NotBlank(message = "登录名不能为空")
    @Size(min = 3, max = 64, message = "登录名长度需在 3-64 之间")
    private String username;

    @Schema(description = "密码", example = "admin123")
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 64, message = "密码长度需在 6-64 之间")
    private String password;
}
