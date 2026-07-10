package com.gac.lms.module.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建角色命令。
 *
 * @author 王茗瑾
 */
@Data
@Schema(description = "创建角色")
public class RoleCreateCmd {

    @Schema(description = "角色编码（唯一，建议大写）", example = "HR")
    @NotBlank(message = "角色编码不能为空")
    @Size(max = 32)
    @Pattern(regexp = "^[A-Z][A-Z0-9_]*$", message = "角色编码需以大写字母开头，仅允许大写字母/数字/下划线")
    private String code;

    @Schema(description = "角色名称", example = "HR")
    @NotBlank(message = "角色名称不能为空")
    @Size(max = 64)
    private String name;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "排序")
    private Integer sort;
}
