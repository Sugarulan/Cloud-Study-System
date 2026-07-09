package com.gac.lms.module.account.controller;

import com.gac.lms.common.response.PageResult;
import com.gac.lms.common.response.Result;
import com.gac.lms.module.account.dto.PermissionAssignCmd;
import com.gac.lms.module.account.dto.RoleCreateCmd;
import com.gac.lms.module.account.dto.RoleUpdateCmd;
import com.gac.lms.module.account.service.RoleService;
import com.gac.lms.module.account.vo.RoleVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 角色管理 Controller（3.3.1）。
 *
 * <p>路径前缀 {@code /api/v1/roles}。</p>
 *
 * @author 王茗瑾
 */
@Tag(name = "角色管理", description = "3.3.1 角色：CRUD + 权限分配")
@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @Operation(summary = "查询所有启用角色（下拉用）")
    @GetMapping("/all")
    public Result<List<RoleVO>> listAll() {
        return Result.ok(roleService.listAll());
    }

    @Operation(summary = "分页查询角色")
    @GetMapping
    public Result<PageResult<RoleVO>> page(
            @Parameter(description = "关键字（匹配 code/name）") @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize) {
        return Result.ok(roleService.page(keyword, pageNum, pageSize));
    }

    @Operation(summary = "角色详情")
    @GetMapping("/{id}")
    public Result<RoleVO> getById(@PathVariable Long id) {
        return Result.ok(roleService.getById(id));
    }

    @Operation(summary = "创建角色")
    @PostMapping
    public Result<RoleVO> create(@Valid @RequestBody RoleCreateCmd cmd) {
        return Result.ok(roleService.create(cmd));
    }

    @Operation(summary = "更新角色（不可改 code）")
    @PutMapping("/{id}")
    public Result<RoleVO> update(@PathVariable Long id,
                                 @Valid @RequestBody RoleUpdateCmd cmd) {
        return Result.ok(roleService.update(id, cmd));
    }

    @Operation(summary = "删除角色（被账号引用时禁止）")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return Result.ok();
    }

    @Operation(summary = "角色分配权限（替换式）")
    @PostMapping("/{id}/permissions")
    public Result<Void> assignPermissions(@PathVariable Long id,
                                          @Valid @RequestBody PermissionAssignCmd cmd) {
        roleService.assignPermissions(id, cmd);
        return Result.ok();
    }
}
