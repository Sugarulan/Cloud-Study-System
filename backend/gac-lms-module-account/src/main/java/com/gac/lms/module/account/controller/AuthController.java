package com.gac.lms.module.account.controller;

import com.gac.lms.common.response.Result;
import com.gac.lms.module.account.dto.LoginCmd;
import com.gac.lms.module.account.dto.LoginResp;
import com.gac.lms.module.account.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证 Controller（3.3.1 账号管理）。
 *
 * <p>提供登录态相关接口，路径前缀 {@code /api/v1/auth}：</p>
 * <ul>
 *   <li>{@code POST /auth/login}   —— 用户名密码登录，返回 JWT</li>
 *   <li>{@code GET  /auth/me}      —— 查询当前登录用户信息</li>
 *   <li>{@code POST /auth/logout}  —— 登出（前端清 token 即可，服务端为预留接口）</li>
 * </ul>
 *
 * <p>以上路径在 {@code SecurityConfig} 中已配置 permitAll，无需 token 即可访问。</p>
 *
 * @author 王茗瑾
 */
@Tag(name = "认证管理", description = "3.3.1 账号：登录态 / 当前用户")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginResp> login(@Valid @RequestBody LoginCmd cmd) {
        return Result.ok(authService.login(cmd));
    }

    @Operation(summary = "查询当前登录用户")
    @GetMapping("/me")
    public Result<LoginResp> me() {
        return Result.ok(authService.currentUserInfo());
    }

    @Operation(summary = "登出（前端清 token 即可）")
    @PostMapping("/logout")
    public Result<Void> logout() {
        // 当前为无状态 JWT，服务端不需要销毁；预留扩展点（如 token 黑名单）
        return Result.ok();
    }
}
