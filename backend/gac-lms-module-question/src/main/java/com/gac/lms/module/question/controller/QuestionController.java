package com.gac.lms.module.question.controller;

import com.gac.lms.common.response.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 题目管理 Controller（W1 骨架）。
 *
 * <p>W2 完成：题目创建/编辑/删除、属性管理（难度/分类/标签）、答案管理、批量导入导出。</p>
 *
 * @author 王茗瑾
 */
@Tag(name = "题目管理", description = "3.3.3 题目：CRUD/属性/答案/批量导入导出")
@RestController
@RequestMapping("/api/v1/questions")
public class QuestionController {

    @Operation(summary = "健康检查（W1 验收用）")
    @GetMapping("/health")
    public Result<String> health() {
        return Result.ok("question-module-ok");
    }
}
