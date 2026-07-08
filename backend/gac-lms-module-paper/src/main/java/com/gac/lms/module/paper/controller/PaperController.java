package com.gac.lms.module.paper.controller;

import com.gac.lms.common.response.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 试卷管理 Controller（W1 骨架）。
 *
 * <p>W2 完成：试卷创建/编辑/删除、抽题组卷、试卷发布。</p>
 *
 * @author 王茗瑾
 */
@Tag(name = "试卷管理", description = "3.3.4 试卷：CRUD/抽题组卷/发布")
@RestController
@RequestMapping("/api/v1/papers")
public class PaperController {

    @Operation(summary = "健康检查（W1 验收用）")
    @GetMapping("/health")
    public Result<String> health() {
        return Result.ok("paper-module-ok");
    }
}
