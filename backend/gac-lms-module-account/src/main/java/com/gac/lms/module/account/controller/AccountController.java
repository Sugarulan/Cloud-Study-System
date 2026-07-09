package com.gac.lms.module.account.controller;

import com.gac.lms.common.response.PageResult;
import com.gac.lms.common.response.Result;
import com.gac.lms.module.account.dto.AccountCreateCmd;
import com.gac.lms.module.account.dto.AccountQuery;
import com.gac.lms.module.account.dto.AccountUpdateCmd;
import com.gac.lms.module.account.dto.PasswordChangeCmd;
import com.gac.lms.module.account.dto.PasswordResetCmd;
import com.gac.lms.module.account.dto.RoleAssignCmd;
import com.gac.lms.module.account.service.AccountService;
import com.gac.lms.module.account.vo.AccountVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 账号管理 Controller（3.3.1）。
 *
 * <p>路径前缀 {@code /api/v1/accounts}（继承自类级 {@code @RequestMapping}）。</p>
 *
 * @author 王茗瑾
 */
@Tag(name = "账号管理", description = "3.3.1 账号：CRUD / 启停 / 密码 / 角色分配")
@Slf4j
@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @Operation(summary = "健康检查（W1 验收用）")
    @GetMapping("/health")
    public Result<String> health() {
        return Result.ok("account-module-ok");
    }

    @Operation(summary = "分页查询账号")
    @GetMapping
    public Result<PageResult<AccountVO>> page(AccountQuery query) {
        return Result.ok(accountService.page(query));
    }

    @Operation(summary = "账号详情")
    @GetMapping("/{id}")
    public Result<AccountVO> getById(@Parameter(description = "账号 ID") @PathVariable Long id) {
        return Result.ok(accountService.getById(id));
    }

    @Operation(summary = "创建账号")
    @PostMapping
    public Result<AccountVO> create(@Valid @RequestBody AccountCreateCmd cmd) {
        return Result.ok(accountService.create(cmd));
    }

    @Operation(summary = "更新账号（邮箱/手机/状态）")
    @PutMapping("/{id}")
    public Result<AccountVO> update(@PathVariable Long id,
                                    @Valid @RequestBody AccountUpdateCmd cmd) {
        return Result.ok(accountService.update(id, cmd));
    }

    @Operation(summary = "启用账号")
    @PostMapping("/{id}/enable")
    public Result<Void> enable(@PathVariable Long id) {
        accountService.enable(id);
        return Result.ok();
    }

    @Operation(summary = "停用账号")
    @PostMapping("/{id}/disable")
    public Result<Void> disable(@PathVariable Long id) {
        accountService.disable(id);
        return Result.ok();
    }

    @Operation(summary = "管理员重置密码")
    @PostMapping("/{id}/reset-password")
    public Result<Void> resetPassword(@PathVariable Long id,
                                      @Valid @RequestBody PasswordResetCmd cmd) {
        accountService.resetPassword(id, cmd);
        return Result.ok();
    }

    @Operation(summary = "修改自己的密码（需旧密码）")
    @PostMapping("/{id}/change-password")
    public Result<Void> changePassword(@PathVariable Long id,
                                       @Valid @RequestBody PasswordChangeCmd cmd) {
        accountService.changePassword(id, cmd);
        return Result.ok();
    }

    @Operation(summary = "查询账号的角色列表")
    @GetMapping("/{id}/roles")
    public Result<List<AccountVO.RoleRefVO>> listRoles(@PathVariable Long id) {
        return Result.ok(accountService.listRoles(id));
    }

    @Operation(summary = "分配角色（替换式，返回成功分配的角色数）")
    @PostMapping("/{id}/roles")
    public Result<Integer> assignRoles(@PathVariable Long id,
                                       @Valid @RequestBody RoleAssignCmd cmd) {
        int count = accountService.assignRoles(id, cmd);
        log.info("[API] assignRoles response: accountId={} count={}", id, count);
        return Result.ok(count);
    }
}
