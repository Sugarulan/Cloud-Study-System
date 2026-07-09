package com.gac.lms.module.exam.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 单题暂存请求。
 *
 * @author 方雨菲
 */
@Data
@Schema(description = "单题暂存请求")
public class SaveAnswerRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 题目 ID */
    @NotNull(message = "题目 ID 不能为空")
    @Schema(description = "题目 ID", example = "5")
    private Long questionId;

    /** 答案（JSON 字符串，覆盖题型：单选=A，多选=[A,C]，填空=文本，问答=长文本） */
    @NotBlank(message = "答案不能为空")
    @Schema(description = "答案内容", example = "B")
    private String answer;

    /** 当前快照版本号（乐观锁） */
    @NotNull(message = "版本号不能为空")
    @Schema(description = "当前快照版本号", example = "3")
    private Integer version;
}
