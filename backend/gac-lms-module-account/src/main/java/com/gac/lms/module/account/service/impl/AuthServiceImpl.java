package com.gac.lms.module.account.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gac.lms.common.exception.BusinessException;
import com.gac.lms.common.response.ErrorCode;
import com.gac.lms.infrastructure.security.JwtProperties;
import com.gac.lms.infrastructure.security.JwtUtil;
import com.gac.lms.infrastructure.security.LoginUser;
import com.gac.lms.infrastructure.security.SecurityContextHelper;
import com.gac.lms.module.account.dto.LoginCmd;
import com.gac.lms.module.account.dto.LoginResp;
import com.gac.lms.module.account.entity.Account;
import com.gac.lms.module.account.entity.AccountRole;
import com.gac.lms.module.account.entity.Permission;
import com.gac.lms.module.account.entity.Role;
import com.gac.lms.module.account.entity.RolePermission;
import com.gac.lms.module.account.mapper.AccountMapper;
import com.gac.lms.module.account.mapper.AccountRoleMapper;
import com.gac.lms.module.account.mapper.PermissionMapper;
import com.gac.lms.module.account.mapper.RoleMapper;
import com.gac.lms.module.account.mapper.RolePermissionMapper;
import com.gac.lms.module.account.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 认证服务实现。
 *
 * <p>登录流程：</p>
 * <ol>
 *   <li>按 username 查账号（带 status 校验）</li>
 *   <li>BCrypt 校验密码</li>
 *   <li>查账号的角色编码列表（通过 account_role → role）</li>
 *   <li>查角色的权限编码列表（通过 role_permission → permission）</li>
 *   <li>生成 JWT，写回 last_login_at</li>
 * </ol>
 *
 * <p><b>设计取舍：</b>本服务不查 Person 详情，避免 account 模块反向依赖 person 模块。
 * 如需展示真实姓名，前端用返回的 userId 调 {@code GET /persons/{id}}。</p>
 *
 * @author 王茗瑾
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AccountMapper accountMapper;
    private final AccountRoleMapper accountRoleMapper;
    private final RoleMapper roleMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final PermissionMapper permissionMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final JwtProperties jwtProps;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginResp login(LoginCmd cmd) {
        // 1) 查账号
        Account account = accountMapper.selectOne(
                new LambdaQueryWrapper<Account>()
                        .eq(Account::getUsername, cmd.getUsername())
        );
        if (account == null) {
            log.warn("[Auth] login failed: username not found, {}", cmd.getUsername());
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户名或密码错误");
        }
        // 2) 校验状态
        if (account.getStatus() != null && account.getStatus() == 0) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "账号已停用，请联系管理员");
        }
        // 3) 校验密码
        if (!passwordEncoder.matches(cmd.getPassword(), account.getPasswordHash())) {
            log.warn("[Auth] login failed: bad password, {}", cmd.getUsername());
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户名或密码错误");
        }

        // 4) 查角色编码
        List<String> roles = listRoleCodes(account.getId());

        // 5) 查权限编码
        List<String> permissions = listPermissions(roles);

        // 6) displayName：暂用 username（真实姓名走 /persons/{id}）
        String displayName = account.getUsername();

        // 7) 装配 LoginUser 并签发 Token
        LoginUser user = new LoginUser(
                account.getId(),
                account.getUsername(),
                displayName,
                roles,
                permissions
        );
        String token = jwtUtil.generate(user);

        // 8) 写回 last_login_at
        Account update = new Account();
        update.setId(account.getId());
        update.setLastLoginAt(LocalDateTime.now());
        accountMapper.updateById(update);

        log.info("[Auth] login ok: userId={} username={} roles={}",
                account.getId(), account.getUsername(), roles);

        return LoginResp.builder()
                .token(token)
                .expireMinutes(jwtProps.getExpireMinutes())
                .userId(account.getId())
                .username(account.getUsername())
                .displayName(displayName)
                .roles(roles)
                .permissions(permissions)
                .build();
    }

    @Override
    public LoginResp currentUserInfo() {
        LoginUser user = SecurityContextHelper.currentUser();
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        // 重新从 DB 拉一遍 roles/permissions（保持最新）
        List<String> roles = listRoleCodes(user.getUserId());
        List<String> permissions = listPermissions(roles);

        return LoginResp.builder()
                .token(null)
                .expireMinutes(jwtProps.getExpireMinutes())
                .userId(user.getUserId())
                .username(user.getUsername())
                .displayName(user.getDisplayName() != null ? user.getDisplayName() : user.getUsername())
                .roles(roles)
                .permissions(permissions)
                .build();
    }

    // ============== 私有查询 ==============

    private List<String> listRoleCodes(Long accountId) {
        List<AccountRole> ars = accountRoleMapper.selectList(
                new LambdaQueryWrapper<AccountRole>().eq(AccountRole::getAccountId, accountId)
        );
        if (ars.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> roleIds = ars.stream().map(AccountRole::getRoleId).collect(Collectors.toList());
        return roleMapper.selectBatchIds(roleIds).stream()
                .map(Role::getCode)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
    }

    private List<String> listPermissions(List<String> roleCodes) {
        if (roleCodes == null || roleCodes.isEmpty()) {
            return Collections.emptyList();
        }
        // role.code → role.id
        List<Role> roles = roleMapper.selectList(
                new LambdaQueryWrapper<Role>().in(Role::getCode, roleCodes)
        );
        if (roles.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> roleIds = roles.stream().map(Role::getId).collect(Collectors.toList());
        // role.id → permission.id
        List<RolePermission> rps = rolePermissionMapper.selectList(
                new LambdaQueryWrapper<RolePermission>().in(RolePermission::getRoleId, roleIds)
        );
        if (rps.isEmpty()) {
            return Collections.emptyList();
        }
        // permission.id → permission.code
        List<Long> permIds = rps.stream()
                .map(RolePermission::getPermissionId)
                .distinct()
                .collect(Collectors.toList());
        return permissionMapper.selectBatchIds(permIds).stream()
                .map(Permission::getCode)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }
}
