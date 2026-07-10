package com.gac.lms.module.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新账号命令（邮箱 / 手机 / 状态）。
 *
 * <p>用户名、密码不允许通过此接口修改。</p>
 *
 * @author 王茗瑾
 */
@Data
@Schema(description = "更新账号")
public class AccountUpdateCmd {

    @Schema(description = "邮箱", example = "zhangsan@gac-lms.local")
    @Size(max = 128)
    private String email;

    @Schema(description = "手机号", example = "13800000099")
    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Schema(description = "状态：0=禁用，1=启用")
    private Integer status;
}
