package com.gac.lms.module.person.controller;

import com.gac.lms.common.response.PageResult;
import com.gac.lms.common.response.Result;
import com.gac.lms.module.person.dto.PersonCreateCmd;
import com.gac.lms.module.person.dto.PersonQuery;
import com.gac.lms.module.person.dto.PersonUpdateCmd;
import com.gac.lms.module.person.service.PersonService;
import com.gac.lms.module.person.vo.PersonVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 人员信息管理 Controller（3.3.2）。
 *
 * <p>路径前缀 {@code /api/v1/persons}。</p>
 *
 * @author 王茗瑾
 */
@Tag(name = "人员信息管理", description = "3.3.2 人员：CRUD / 部门分配 / 账号关联")
@RestController
@RequestMapping("/api/v1/persons")
@RequiredArgsConstructor
public class PersonController {

    private final PersonService personService;

    @Operation(summary = "健康检查（W1 验收用）")
    @GetMapping("/health")
    public Result<String> health() {
        return Result.ok("person-module-ok");
    }

    @Operation(summary = "分页查询人员")
    @GetMapping
    public Result<PageResult<PersonVO>> page(PersonQuery query) {
        return Result.ok(personService.page(query));
    }

    @Operation(summary = "人员详情")
    @GetMapping("/{id}")
    public Result<PersonVO> getById(@PathVariable Long id) {
        return Result.ok(personService.getById(id));
    }

    @Operation(summary = "创建人员（可选同时创建账号）")
    @PostMapping
    public Result<PersonVO> create(@Valid @RequestBody PersonCreateCmd cmd) {
        return Result.ok(personService.create(cmd));
    }

    @Operation(summary = "更新人员")
    @PutMapping("/{id}")
    public Result<PersonVO> update(@PathVariable Long id,
                                   @Valid @RequestBody PersonUpdateCmd cmd) {
        return Result.ok(personService.update(id, cmd));
    }

    @Operation(summary = "离职（软删）")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        personService.delete(id);
        return Result.ok();
    }

    @Operation(summary = "查询人员的部门列表")
    @GetMapping("/{id}/departments")
    public Result<List<PersonVO.DepartmentRefVO>> listDepartments(@PathVariable Long id) {
        return Result.ok(personService.listDepartments(id));
    }

    @Operation(summary = "分配部门（替换式，含主部门）")
    @PostMapping("/{id}/departments")
    public Result<Void> assignDepartments(
            @PathVariable Long id,
            @RequestBody AssignDepartmentsRequest req) {
        personService.assignDepartments(id, req.getDepartmentIds(), req.getPrimaryDepartmentId());
        return Result.ok();
    }

    /** 分配部门请求体（内嵌） */
    @lombok.Data
    public static class AssignDepartmentsRequest {
        private List<Long> departmentIds;
        private Long primaryDepartmentId;
    }

    // ============== W3 待补：Excel 导入导出 ==============

    // @PostMapping("/import")
    // public Result<Integer> importPersons(@RequestParam("file") MultipartFile file) {
    //     // TODO W3 EasyExcel
    // }

    // @GetMapping("/export")
    // public void exportPersons(PersonQuery query, HttpServletResponse response) {
    //     // TODO W3 EasyExcel
    // }
}
