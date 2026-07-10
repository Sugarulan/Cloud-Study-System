package com.gac.lms.module.question.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 批量删除请求体。
 *
 * @author 王茗瑾
 */
@Data
@Schema(description = "批量删除请求体")
public class BatchDeleteCmd {

    @NotEmpty(message = "ids 不能为空")
    @Schema(description = "要删除的题目 ID 列表")
    private List<Long> ids;
}
