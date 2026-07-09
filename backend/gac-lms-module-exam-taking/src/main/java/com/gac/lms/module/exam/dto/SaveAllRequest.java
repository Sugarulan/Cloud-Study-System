package com.gac.lms.module.exam.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * 批量暂存请求。
 *
 * @author 方雨菲
 */
@Data
@Schema(description = "批量暂存请求")
public class SaveAllRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 题目 ID -> 答案 */
    @NotEmpty(message = "答案不能为空")
    @Schema(description = "题目 ID 与答案的映射")
    private Map<Long, String> answers;

    /** 当前快照版本号 */
    @NotNull(message = "版本号不能为空")
    @Schema(description = "当前快照版本号", example = "3")
    private Integer version;
}
