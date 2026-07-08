package com.gac.lms.module.account.controller;

import com.gac.lms.common.response.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 账号管理 Controller（W1 骨架）。
 *
 * <p>W2 完成：账号创建/更新/停用、角色分配、密码策略与重置、登录鉴权。</p>
 *
 * @author 王茗瑾
 */
@Tag(name = "账号管理", description = "3.3.1 账号：创建/更新/停用、角色管理、密码策略")
@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    @Operation(summary = "健康检查（W1 验收用）")
    @GetMapping("/health")
    public Result<String> health() {
        return Result.ok("account-module-ok");
    }
}
