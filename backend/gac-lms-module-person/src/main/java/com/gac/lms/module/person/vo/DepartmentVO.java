package com.gac.lms.module.person.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 部门出参 VO。
 *
 * <p>树形查询时 {@code children} 递归填充。</p>
 *
 * @author 王茗瑾
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "部门信息")
public class DepartmentVO {

    @Schema(description = "部门 ID")
    private Long id;

    @Schema(description = "父部门 ID（0 = 顶级）")
    private Long parentId;

    @Schema(description = "部门名称")
    private String name;

    @Schema(description = "部门编码")
    private String code;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "部门负责人 person_id")
    private Long leaderId;

    @Schema(description = "状态：0=禁用 1=启用")
    private Integer status;

    @Schema(description = "子部门列表（树形查询时填充）")
    private List<DepartmentVO> children;
}
