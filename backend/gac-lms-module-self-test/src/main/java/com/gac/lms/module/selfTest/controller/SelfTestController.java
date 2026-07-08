package com.gac.lms.module.selfTest.controller;

import com.gac.lms.common.response.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 个人测评 Controller（W1 骨架）。
 *
 * <p>W2 完成：待考/已考列表、错题本。W4 完成：AI 错题解析。</p>
 *
 * @author 方雨菲
 */
@Tag(name = "个人测评", description = "3.3.9 我的考试 / 错题本 / AI 解析")
@RestController
@RequestMapping("/api/v1/self-test")
public class SelfTestController {

    @Operation(summary = "健康检查（W1 验收用）")
    @GetMapping("/health")
    public Result<String> health() {
        return Result.ok("self-test-module-ok");
    }
}
