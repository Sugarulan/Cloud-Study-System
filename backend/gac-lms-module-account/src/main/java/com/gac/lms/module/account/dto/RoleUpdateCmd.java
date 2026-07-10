package com.gac.lms.module.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新角色命令。
 *
 * <p>不允许通过此接口修改 code（编码通常作为系统约定）。</p>
 *
 * @author 王茗瑾
 */
@Data
@Schema(description = "更新角色")
public class RoleUpdateCmd {

    @Schema(description = "角色名称")
    @Size(max = 64)
    private String name;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "状态：0=禁用，1=启用")
    private Integer status;
}
