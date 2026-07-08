package com.gac.lms.module.person.controller;

import com.gac.lms.common.response.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 人员信息管理 Controller（W1 骨架）。
 *
 * <p>W2 完成：人员创建/更新/关联账号、按部门与系统分组、Excel 导入导出。</p>
 *
 * @author 王茗瑾
 */
@Tag(name = "人员信息管理", description = "3.3.2 人员：创建/更新/分组/导入导出")
@RestController
@RequestMapping("/api/v1/persons")
public class PersonController {

    @Operation(summary = "健康检查（W1 验收用）")
    @GetMapping("/health")
    public Result<String> health() {
        return Result.ok("person-module-ok");
    }
}
