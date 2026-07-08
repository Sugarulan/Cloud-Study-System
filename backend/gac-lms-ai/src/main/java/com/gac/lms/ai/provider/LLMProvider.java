package com.gac.lms.ai.provider;

/**
 * LLM Provider 统一接口。
 *
 * <p>所有 AI 调用都通过本接口，便于：</p>
 * <ul>
 *   <li>在 Mock Provider 与真实 Provider 之间无感切换</li>
 *   <li>统一接入 Resilience4j 熔断、限流、监控</li>
 * </ul>
 *
 * @author 方雨菲
 */
public interface LLMProvider {

    /**
     * Provider 类型标识，用于按配置选择具体实现。
     */
    String type();

    /**
     * 同步调用大模型。
     *
     * @param prompt 完整 prompt（包含上下文与指令）
     * @return 大模型返回文本
     */
    String invoke(String prompt);

    /**
     * 带参数的同步调用。
     *
     * @param request 调用参数
     * @return 大模型返回文本
     */
    String invoke(LLMRequest request);

    /**
     * 健康检查：Provider 是否可用。
     *
     * <p>熔断器会基于本方法的结果判断是否切到降级 Provider。</p>
     */
    default boolean isHealthy() {
        return true;
    }
}
