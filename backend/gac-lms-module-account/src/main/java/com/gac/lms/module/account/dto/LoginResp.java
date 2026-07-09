package com.gac.lms.module.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 登录响应。
 *
 * @author 王茗瑾
 */
@Data
@Builder
@Schema(description = "登录响应")
public class LoginResp {

    @Schema(description = "JWT Token", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String token;

    @Schema(description = "Token 有效期（分钟）")
    private Integer expireMinutes;

    @Schema(description = "账号 ID")
    private Long userId;

    @Schema(description = "登录名")
    private String username;

    @Schema(description = "姓名（取自 person 表）")
    private String displayName;

    @Schema(description = "角色编码列表", example = "[\"ADMIN\"]")
    private List<String> roles;

    @Schema(description = "权限编码列表", example = "[\"account:list\", \"account:create\"]")
    private List<String> permissions;
}
