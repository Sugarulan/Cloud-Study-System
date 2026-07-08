package com.gac.lms.module.examMgmt.controller;

import com.gac.lms.common.response.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 考试管理 Controller（W1 骨架）。
 *
 * <p>W2 完成：考试创建/编辑/发布、交卷规则、自动/手动交卷、参考范围、考试周期。</p>
 *
 * @author 王茗瑾
 */
@Tag(name = "考试管理", description = "3.3.5 考试：CRUD/交卷规则/参考范围/考试周期")
@RestController
@RequestMapping("/api/v1/exams")
public class ExamController {

    @Operation(summary = "健康检查（W1 验收用）")
    @GetMapping("/health")
    public Result<String> health() {
        return Result.ok("exam-module-ok");
    }
}
