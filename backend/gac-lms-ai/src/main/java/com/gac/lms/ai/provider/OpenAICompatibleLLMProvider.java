package com.gac.lms.ai.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * OpenAI 兼容协议 Provider —— 用于接入企业内网大模型。
 *
 * <p>绝大多数国产大模型（通义千问、DeepSeek、ChatGLM 等）都提供
 * OpenAI 兼容的 REST API 接口，只需配置 base-url 即可。</p>
 *
 * <p>本实现不依赖 Spring AI SDK，而是直接使用 Spring Boot 内置的 {@link RestClient}
 * 调用 {@code POST {base-url}/chat/completions}，避免 milestone 仓库可达性与版本兼容问题，
 * 做到零外部 SDK 依赖。</p>
 *
 * <p>通过配置项 {@code gac.lms.ai.provider} 启用：
 * <pre>
 *   gac.lms.ai.provider=openai-compatible
 *   gac.lms.ai.openai.base-url=http://internal-llm.gac.local/v1
 *   gac.lms.ai.openai.api-key=sk-xxx
 *   gac.lms.ai.openai.model=qwen-plus
 *   gac.lms.ai.openai.temperature=0.3
 * </pre>
 * </p>
 *
 * @author 方雨菲
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "gac.lms.ai", name = "provider", havingValue = "openai-compatible")
public class OpenAICompatibleLLMProvider implements LLMProvider {

    private final RestClient.Builder clientBuilder;
    private final ObjectMapper objectMapper;

    @Value("${gac.lms.ai.openai.base-url:http://internal-llm.gac.local/v1}")
    private String baseUrl;

    @Value("${gac.lms.ai.openai.api-key:sk-placeholder}")
    private String apiKey;

    @Value("${gac.lms.ai.openai.model:qwen-plus}")
    private String defaultModel;

    @Value("${gac.lms.ai.openai.temperature:0.3}")
    private double defaultTemperature;

    private RestClient restClient;

    public OpenAICompatibleLLMProvider(RestClient.Builder clientBuilder, ObjectMapper objectMapper) {
        this.clientBuilder = clientBuilder;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    void init() {
        this.restClient = clientBuilder
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
        log.info("[OpenAICompatibleLLMProvider] initialized: base-url={}, model={}", baseUrl, defaultModel);
    }

    @Override
    public String type() {
        return "openai-compatible";
    }

    @Override
    public String invoke(String prompt) {
        return doInvoke(defaultModel, null, prompt, defaultTemperature);
    }

    @Override
    public String invoke(LLMRequest request) {
        String model = request.getModel() != null ? request.getModel() : defaultModel;
        double temperature = request.getTemperature() != null ? request.getTemperature() : defaultTemperature;
        return doInvoke(model, request.getSystemPrompt(), request.getPrompt(), temperature);
    }

    /**
     * 调用 OpenAI 兼容的 /chat/completions 接口。
     */
    private String doInvoke(String model, String systemPrompt, String userPrompt, double temperature) {
        long start = System.currentTimeMillis();
        log.debug("[OpenAICompatibleLLMProvider] invoke: model={}, prompt length={}",
                model, userPrompt == null ? 0 : userPrompt.length());

        List<Map<String, String>> messages = new java.util.ArrayList<>();
        if (systemPrompt != null && !systemPrompt.isBlank()) {
            messages.add(Map.of("role", "system", "content", systemPrompt));
        } else {
            messages.add(Map.of("role", "system", "content", "你是广汽云学习系统的智能助手。"));
        }
        messages.add(Map.of("role", "user", "content", userPrompt == null ? "" : userPrompt));

        Map<String, Object> body = Map.of(
                "model", model,
                "messages", messages,
                "temperature", temperature
        );

        try {
            String response = restClient.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);

            String content = extractContent(response);
            log.debug("[OpenAICompatibleLLMProvider] invoke done in {}ms, content length={}",
                    System.currentTimeMillis() - start, content == null ? 0 : content.length());
            return content;
        } catch (Exception ex) {
            log.error("[OpenAICompatibleLLMProvider] invoke failed in {}ms: {}",
                    System.currentTimeMillis() - start, ex.getMessage());
            throw ex;
        }
    }

    /**
     * 从 OpenAI 兼容响应中提取 choices[0].message.content。
     */
    private String extractContent(String response) {
        if (response == null || response.isBlank()) {
            return "";
        }
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode content = root.path("choices").path(0).path("message").path("content");
            return content.isMissingNode() ? "" : content.asText();
        } catch (Exception e) {
            log.warn("[OpenAICompatibleLLMProvider] parse response failed: {}", e.getMessage());
            return "";
        }
    }
}
