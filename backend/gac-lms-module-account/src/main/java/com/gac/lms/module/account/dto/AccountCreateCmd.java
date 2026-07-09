package com.gac.lms.module.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建账号命令。
 *
 * @author 王茗瑾
 */
@Data
@Schema(description = "创建账号")
public class AccountCreateCmd {

    @Schema(description = "登录名（唯一）", example = "zhangsan")
    @NotBlank(message = "登录名不能为空")
    @Size(min = 3, max = 64, message = "登录名长度需在 3-64 之间")
    @Pattern(regexp = "^[a-zA-Z0-9_.-]+$", message = "登录名仅允许字母、数字、点、下划线、连字符")
    private String username;

    @Schema(description = "初始密码（8-64 位，必须包含字母+数字）", example = "Init@1234")
    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 64, message = "密码长度需在 8-64 之间")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "密码必须包含字母和数字")
    private String password;

    @Schema(description = "邮箱", example = "zhangsan@gac-lms.local")
    private String email;

    @Schema(description = "手机号", example = "13800000099")
    private String phone;

    @Schema(description = "初始分配的角色 ID 列表", example = "[3]")
    private java.util.List<Long> roleIds;
}
