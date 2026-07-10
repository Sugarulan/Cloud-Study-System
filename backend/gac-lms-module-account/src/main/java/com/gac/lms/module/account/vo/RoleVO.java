package com.gac.lms.module.account.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色出参 VO（含权限列表）。
 *
 * @author 王茗瑾
 */
@Data
@Builder
@Schema(description = "角色信息")
public class RoleVO {

    @Schema(description = "角色 ID")
    private Long id;

    @Schema(description = "角色编码")
    private String code;

    @Schema(description = "角色名称")
    private String name;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "状态：0=禁用，1=启用")
    private Integer status;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "该角色拥有的权限 ID 列表")
    private List<Long> permissionIds;
}
