package com.gac.lms.module.knowledge.controller;

import com.gac.lms.common.response.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 知识管理 Controller（W1 骨架）。
 *
 * <p>W2 完成：目录/分类/标签、RBAC、状态机、版本控制。W4 完成：AI 文档→题目抽取。</p>
 *
 * @author 方雨菲
 */
@Tag(name = "知识管理", description = "3.3.10 知识库：目录/分类/标签/状态机/版本/AI 抽题")
@RestController
@RequestMapping("/api/v1/knowledge")
public class KnowledgeController {

    @Operation(summary = "健康检查（W1 验收用）")
    @GetMapping("/health")
    public Result<String> health() {
        return Result.ok("knowledge-module-ok");
    }
}
