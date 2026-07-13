package com.gac.lms.module.person.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 人员分页查询条件。
 *
 * @author 王茗瑾
 */
@Data
@Schema(description = "人员分页查询")
public class PersonQuery {

    @Schema(description = "关键字（匹配 employee_no/name/mobile/email）")
    private String keyword;

    @Schema(description = "部门 ID（按部门过滤）")
    private Long departmentId;

    @Schema(description = "状态：0=离职 1=在职")
    private Integer status;

    @Schema(description = "页码", example = "1")
    private Long pageNum = 1L;

    @Schema(description = "每页大小", example = "10")
    private Long pageSize = 10L;
}
