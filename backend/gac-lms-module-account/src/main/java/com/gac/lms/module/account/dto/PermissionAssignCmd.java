package com.gac.lms.module.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 角色分配权限（替换式）。
 *
 * @author 王茗瑾
 */
@Data
@Schema(description = "角色分配权限")
public class PermissionAssignCmd {

    @Schema(description = "权限 ID 列表（空数组=清空所有权限）", example = "[1,2,3]")
    @NotNull(message = "权限列表不能为空")
    private List<Long> permissionIds;
}
