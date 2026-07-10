package com.gac.lms.module.person.controller;

import com.gac.lms.common.response.Result;
import com.gac.lms.module.person.service.DepartmentService;
import com.gac.lms.module.person.vo.DepartmentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 部门管理 Controller（3.3.2）。
 *
 * <p>路径前缀 {@code /api/v1/departments}。</p>
 *
 * @author 王茗瑾
 */
@Tag(name = "部门管理", description = "3.3.2 部门：CRUD + 树形查询")
@RestController
@RequestMapping("/api/v1/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @Operation(summary = "部门树（全量递归）")
    @GetMapping("/tree")
    public Result<List<DepartmentVO>> tree() {
        return Result.ok(departmentService.tree());
    }

    @Operation(summary = "所有部门（扁平，下拉用）")
    @GetMapping("/all")
    public Result<List<DepartmentVO>> listAll() {
        return Result.ok(departmentService.listAll());
    }

    @Operation(summary = "新建部门")
    @PostMapping
    public Result<DepartmentVO> create(
            @Parameter(description = "部门名称") @RequestParam String name,
            @Parameter(description = "部门编码") @RequestParam(required = false) String code,
            @Parameter(description = "父部门 ID，0 = 顶级") @RequestParam(defaultValue = "0") Long parentId,
            @Parameter(description = "排序") @RequestParam(required = false, defaultValue = "0") Integer sort,
            @Parameter(description = "部门负责人 person_id") @RequestParam(required = false) Long leaderId) {
        return Result.ok(departmentService.create(name, code, parentId, sort, leaderId));
    }

    @Operation(summary = "更新部门")
    @PutMapping("/{id}")
    public Result<DepartmentVO> update(
            @PathVariable Long id,
            @Parameter(description = "新名称") @RequestParam(required = false) String name,
            @Parameter(description = "新编码") @RequestParam(required = false) String code,
            @Parameter(description = "新父部门 ID") @RequestParam(required = false) Long parentId,
            @Parameter(description = "新排序") @RequestParam(required = false) Integer sort,
            @Parameter(description = "新负责人 person_id") @RequestParam(required = false) Long leaderId) {
        return Result.ok(departmentService.update(id, name, code, parentId, sort, leaderId));
    }

    @Operation(summary = "删除部门（无子部门且无人员）")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        departmentService.delete(id);
        return Result.ok();
    }
}
