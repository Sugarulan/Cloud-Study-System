package com.gac.lms.ai.provider;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Mock Provider —— 兜底实现，用于：
 * <ul>
 *   <li>开发环境无 AI 接入时跑通主流程</li>
 *   <li>真实 Provider 故障时的降级方案</li>
 *   <li>单元测试</li>
 * </ul>
 *
 * @author 方雨菲
 */
@Slf4j
@Component
public class MockLLMProvider implements LLMProvider {

    @Override
    public String type() {
        return "mock";
    }

    @Override
    public String invoke(String prompt) {
        log.debug("[MockLLMProvider] invoke: prompt length={}", prompt == null ? 0 : prompt.length());
        // 仅返回占位文本，便于主流程跑通
        return "{\"score\": 0, \"comment\": \"Mock Provider 暂未评阅，请使用人工评卷。\"}";
    }

    @Override
    public String invoke(LLMRequest request) {
        return invoke(request.getPrompt());
    }
}
