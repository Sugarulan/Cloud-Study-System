package com.gac.lms.module.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 给账号分配角色（替换式：清空旧分配，写入新分配）。
 *
 * @author 王茗瑾
 */
@Data
@Schema(description = "账号分配角色")
public class RoleAssignCmd {

    @Schema(description = "角色 ID 列表（空数组=清空所有角色）", example = "[2,3]")
    @NotNull(message = "角色列表不能为空")
    private List<Long> roleIds;
}
