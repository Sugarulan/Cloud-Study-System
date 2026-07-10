package com.gac.lms.module.account.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 账号出参 VO（不包含 passwordHash）。
 *
 * @author 王茗瑾
 */
@Data
@Builder
@Schema(description = "账号信息")
public class AccountVO {

    @Schema(description = "账号 ID")
    private Long id;

    @Schema(description = "登录名")
    private String username;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "状态：0=禁用，1=启用")
    private Integer status;

    @Schema(description = "最近登录时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginAt;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "分配的角色列表")
    private List<RoleRefVO> roles;

    /** 角色简要信息（内嵌） */
    @Data
    @Builder
    @Schema(description = "角色简要")
    public static class RoleRefVO {
        @Schema(description = "角色 ID")
        private Long id;
        @Schema(description = "角色编码")
        private String code;
        @Schema(description = "角色名称")
        private String name;
    }
}
