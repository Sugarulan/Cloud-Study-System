package com.gac.lms.module.grade.controller;

import com.gac.lms.common.response.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 成绩管理 Controller（W1 骨架）。
 *
 * <p>W2 完成：多条件筛选、统计（分布/通过率/均分）、Excel 导出。</p>
 *
 * @author 方雨菲
 */
@Tag(name = "成绩管理", description = "3.3.7 成绩：查看/筛选/统计/导出")
@RestController
@RequestMapping("/api/v1/grades")
public class GradeController {

    @Operation(summary = "健康检查（W1 验收用）")
    @GetMapping("/health")
    public Result<String> health() {
        return Result.ok("grade-module-ok");
    }
}
