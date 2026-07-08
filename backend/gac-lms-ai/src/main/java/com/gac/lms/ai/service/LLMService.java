package com.gac.lms.ai.service;

import com.gac.lms.ai.provider.LLMProvider;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * AI 服务门面 —— 所有业务模块都通过本类调用 AI。
 *
 * <p>特性：</p>
 * <ul>
 *   <li>熔断：连续失败自动切到 {@code fallback} 方法（指向 Mock Provider）</li>
 *   <li>降级：业务可选择 {@link #invokeSafely} 捕获异常返回空字符串</li>
 *   <li>可观测：日志埋点，后续接入 Micrometer</li>
 * </ul>
 *
 * @author 方雨菲
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LLMService {

    public static final String CB_NAME = "llmService";

    private final LLMProvider primaryProvider;

    @Qualifier("mockLLMProvider")
    private final LLMProvider fallbackProvider;

    /**
     * 同步调用 AI，启用熔断。
     *
     * <p>熔断打开后会自动调用 fallback 方法（即 Mock Provider）。</p>
     */
    @CircuitBreaker(name = CB_NAME, fallbackMethod = "invokeFallback")
    public String invoke(String prompt) {
        log.debug("[LLMService] invoking primary provider: {}", primaryProvider.type());
        return primaryProvider.invoke(prompt);
    }

    /**
     * 熔断 fallback：使用 Mock Provider。
     */
    @SuppressWarnings("unused")
    private String invokeFallback(String prompt, Throwable ex) {
        log.warn("[LLMService] primary provider failed, fallback to mock. cause={}", ex.getMessage());
        return fallbackProvider.invoke(prompt);
    }

    /**
     * 安全调用：即使 AI 不可用也不会抛异常，返回空字符串。
     *
     * <p>适用于"AI 是增强能力，非核心链路"的场景（如错题解析）。</p>
     */
    public String invokeSafely(String prompt) {
        try {
            return invoke(prompt);
        } catch (Exception ex) {
            log.error("[LLMService] invoke failed even with fallback", ex);
            return "";
        }
    }
}
