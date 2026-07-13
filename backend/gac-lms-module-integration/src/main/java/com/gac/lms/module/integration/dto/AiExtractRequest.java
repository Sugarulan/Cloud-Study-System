package com.gac.lms.module.integration.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * AI 文档 → 题目抽取请求。
 *
 * <p>W2 阶段：调用 AI Mock 返回占位题。</p>
 * <p>W4 阶段：接入企业内网大模型，使用专业 Prompt。</p>
 *
 * @author 方雨菲
 */
@Data
@Schema(description = "AI 文档抽题请求")
public class AiExtractRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull
    private Long docId;

    /** 期望生成的题目数量，默认 5 */
    private Integer expectedCount = 5;

    /** 题目类型偏好，逗号分隔（SINGLE,MULTI,JUDGE,FILL,ESSAY） */
    private String preferredTypes;
}
