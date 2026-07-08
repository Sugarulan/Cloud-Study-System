package com.gac.lms.ai.provider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * OpenAI 兼容协议 Provider —— 用于接入企业内网大模型。
 *
 * <p>绝大多数国产大模型（通义千问、DeepSeek、ChatGLM 等）都提供
 * OpenAI 兼容的 REST API 接口，只需配置 base-url 即可。</p>
 *
 * <p>通过配置项 {@code gac.lms.ai.provider} 启用：
 * <pre>
 *   gac.lms.ai.provider=openai-compatible
 *   spring.ai.openai.base-url=http://internal-llm.gac.local/v1
 *   spring.ai.openai.api-key=sk-xxx
 *   spring.ai.openai.chat.options.model=qwen-plus
 * </pre>
 * </p>
 *
 * @author 方雨菲
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "gac.lms.ai", name = "provider", havingValue = "openai-compatible")
public class OpenAICompatibleLLMProvider implements LLMProvider {

    private final ChatClient chatClient;

    @Override
    public String type() {
        return "openai-compatible";
    }

    @Override
    public String invoke(String prompt) {
        log.debug("[OpenAICompatibleLLMProvider] invoke: prompt length={}", prompt == null ? 0 : prompt.length());
        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }

    @Override
    public String invoke(LLMRequest request) {
        return chatClient.prompt()
                .system(request.getSystemPrompt() != null ? request.getSystemPrompt() : "你是广汽云学习系统的智能助手。")
                .user(request.getPrompt())
                .call()
                .content();
    }
}
