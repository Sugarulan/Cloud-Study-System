package com.gac.lms.module.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 账号分页查询条件。
 *
 * @author 王茗瑾
 */
@Data
@Schema(description = "账号查询条件")
public class AccountQuery {

    @Schema(description = "页码（从 1 开始）", example = "1")
    private Long pageNum = 1L;

    @Schema(description = "每页大小", example = "10")
    private Long pageSize = 10L;

    @Schema(description = "关键字：匹配 username / email / phone", example = "admin")
    private String keyword;

    @Schema(description = "状态：0=禁用，1=启用；不传查全部")
    private Integer status;
}
