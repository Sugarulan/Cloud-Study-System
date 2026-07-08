package com.gac.lms.ai.provider;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * LLM 调用参数。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LLMRequest {

    /** 完整 prompt */
    private String prompt;

    /** 系统提示词（可选） */
    private String systemPrompt;

    /** 温度参数 0~1，越高越发散 */
    private Double temperature;

    /** 最大输出 token */
    private Integer maxTokens;

    /** 模型名称（不同 Provider 可能支持不同模型） */
    private String model;

    /** 超时时间（毫秒），默认 30s */
    private Integer timeoutMs;
}
