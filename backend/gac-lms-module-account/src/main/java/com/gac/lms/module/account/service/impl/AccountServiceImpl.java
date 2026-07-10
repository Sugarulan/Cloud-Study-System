package com.gac.lms.module.account.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gac.lms.common.exception.BusinessException;
import com.gac.lms.common.response.ErrorCode;
import com.gac.lms.common.response.PageResult;
import com.gac.lms.infrastructure.security.SecurityContextHelper;
import com.gac.lms.module.account.dto.AccountCreateCmd;
import com.gac.lms.module.account.dto.AccountQuery;
import com.gac.lms.module.account.dto.AccountUpdateCmd;
import com.gac.lms.module.account.dto.PasswordChangeCmd;
import com.gac.lms.module.account.dto.PasswordResetCmd;
import com.gac.lms.module.account.dto.RoleAssignCmd;
import com.gac.lms.module.account.entity.Account;
import com.gac.lms.module.account.entity.AccountRole;
import com.gac.lms.module.account.entity.Role;
import com.gac.lms.module.account.mapper.AccountMapper;
import com.gac.lms.module.account.mapper.AccountRoleMapper;
import com.gac.lms.module.account.mapper.RoleMapper;
import com.gac.lms.module.account.service.AccountService;
import com.gac.lms.module.account.vo.AccountVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 账号业务实现。
 *
 * @author 王茗瑾
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountMapper accountMapper;
    private final AccountRoleMapper accountRoleMapper;
    private final RoleMapper roleMapper;
    private final PasswordEncoder passwordEncoder;
    private final TransactionTemplate txTemplate;

    @Override
    public PageResult<AccountVO> page(AccountQuery query) {
        Page<Account> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<Account> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(Account::getUsername, query.getKeyword())
                    .or().like(Account::getEmail, query.getKeyword())
                    .or().like(Account::getPhone, query.getKeyword()));
        }
        if (query.getStatus() != null) {
            wrapper.eq(Account::getStatus, query.getStatus());
        }
        wrapper.orderByDesc(Account::getCreateTime);

        Page<Account> result = accountMapper.selectPage(page, wrapper);
        List<AccountVO> records = result.getRecords().stream()
                .map(this::toVOWithoutRoles)
                .collect(Collectors.toList());

        // 批量填充 roles（避免 N+1）
        if (!records.isEmpty()) {
            fillRoles(records);
        }

        return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), records);
    }

    @Override
    public AccountVO getById(Long id) {
        Account a = mustGet(id);
        AccountVO vo = toVOWithoutRoles(a);
        fillRoles(List.of(vo));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AccountVO create(AccountCreateCmd cmd) {
        // 1) 唯一性校验
        Long exists = accountMapper.selectCount(
                new LambdaQueryWrapper<Account>().eq(Account::getUsername, cmd.getUsername())
        );
        if (exists > 0) {
            throw new BusinessException(ErrorCode.DATA_ALREADY_EXISTS, "登录名已存在");
        }
        // 2) 插入账号
        Account a = new Account();
        a.setUsername(cmd.getUsername());
        a.setPasswordHash(passwordEncoder.encode(cmd.getPassword()));
        a.setEmail(cmd.getEmail());
        a.setPhone(cmd.getPhone());
        a.setStatus(1);  // 默认启用
        accountMapper.insert(a);
        log.info("[Account] created: id={} username={}", a.getId(), a.getUsername());

        // 3) 分配角色
        if (cmd.getRoleIds() != null && !cmd.getRoleIds().isEmpty()) {
            assignRolesInternal(a.getId(), cmd.getRoleIds());
        }

        return getById(a.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AccountVO update(Long id, AccountUpdateCmd cmd) {
        Account a = mustGet(id);
        // 不允许通过此接口修改 username / password
        a.setEmail(cmd.getEmail());
        a.setPhone(cmd.getPhone());
        if (cmd.getStatus() != null) {
            a.setStatus(cmd.getStatus());
        }
        accountMapper.updateById(a);
        log.info("[Account] updated: id={}", id);
        return getById(id);
    }

    @Override
    public void enable(Long id) {
        updateStatus(id, 1);
    }

    @Override
    public void disable(Long id) {
        updateStatus(id, 0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(Long id, PasswordResetCmd cmd) {
        mustGet(id);
        Account update = new Account();
        update.setId(id);
        update.setPasswordHash(passwordEncoder.encode(cmd.getNewPassword()));
        accountMapper.updateById(update);
        log.info("[Account] reset password: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(Long id, PasswordChangeCmd cmd) {
        // 只能改自己的密码
        Long currentUserId = SecurityContextHelper.currentUserId();
        if (!currentUserId.equals(id)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只能修改自己的密码");
        }
        Account a = mustGet(id);
        if (!passwordEncoder.matches(cmd.getOldPassword(), a.getPasswordHash())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "旧密码不正确");
        }
        Account update = new Account();
        update.setId(id);
        update.setPasswordHash(passwordEncoder.encode(cmd.getNewPassword()));
        accountMapper.updateById(update);
        log.info("[Account] changed password: id={}", id);
    }

    @Override
    public List<AccountVO.RoleRefVO> listRoles(Long accountId) {
        mustGet(accountId);
        List<AccountRole> ars = accountRoleMapper.selectList(
                new LambdaQueryWrapper<AccountRole>().eq(AccountRole::getAccountId, accountId)
        );
        if (ars.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> roleIds = ars.stream().map(AccountRole::getRoleId).collect(Collectors.toList());
        return roleMapper.selectBatchIds(roleIds).stream()
                .map(r -> AccountVO.RoleRefVO.builder()
                        .id(r.getId()).code(r.getCode()).name(r.getName()).build())
                .collect(Collectors.toList());
    }

    @Override
    public int assignRoles(Long accountId, RoleAssignCmd cmd) {
        log.info("[Account] assignRoles start: accountId={} roleIds={}", accountId, cmd.getRoleIds());

        Integer count = txTemplate.execute(status -> {
            // 1) 校验账号存在
            if (accountMapper.selectById(accountId) == null) {
                throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "账号不存在");
            }
            // 2) 清旧关联
            accountRoleMapper.delete(
                    new LambdaQueryWrapper<AccountRole>().eq(AccountRole::getAccountId, accountId)
            );
            // 3) 去重 + 过滤 null
            Set<Long> distinct = cmd.getRoleIds() == null ? java.util.Set.of()
                    : cmd.getRoleIds().stream().filter(Objects::nonNull).collect(Collectors.toSet());
            if (distinct.isEmpty()) {
                return 0;
            }
            // 4) 校验角色都存在
            Long existing = roleMapper.selectCount(
                    new LambdaQueryWrapper<Role>().in(Role::getId, distinct)
            );
            if (existing != distinct.size()) {
                throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "部分角色 ID 不存在");
            }
            // 5) 写入新关联
            for (Long rid : distinct) {
                AccountRole ar = new AccountRole();
                ar.setAccountId(accountId);
                ar.setRoleId(rid);
                accountRoleMapper.insert(ar);
            }
            return distinct.size();
        });

        log.info("[Account] assignRoles done: accountId={} count={}", accountId, count);
        return count == null ? 0 : count;
    }

    // ============== 私有方法 ==============

    private Account mustGet(Long id) {
        Account a = accountMapper.selectById(id);
        if (a == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "账号不存在");
        }
        return a;
    }

    private void updateStatus(Long id, Integer status) {
        mustGet(id);
        Account update = new Account();
        update.setId(id);
        update.setStatus(status);
        accountMapper.updateById(update);
        log.info("[Account] status changed: id={} status={}", id, status);
    }

    private void assignRolesInternal(Long accountId, List<Long> roleIds) {
        // 清旧
        accountRoleMapper.delete(
                new LambdaQueryWrapper<AccountRole>().eq(AccountRole::getAccountId, accountId)
        );
        // 写新（去重 + 过滤 null）
        Set<Long> distinct = roleIds.stream().filter(Objects::nonNull).collect(Collectors.toSet());
        if (distinct.isEmpty()) {
            return;
        }
        // 校验 roleIds 真实存在
        List<Role> roles = roleMapper.selectBatchIds(distinct);
        if (roles.size() != distinct.size()) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "部分角色 ID 不存在");
        }
        distinct.forEach(rid -> {
            AccountRole ar = new AccountRole();
            ar.setAccountId(accountId);
            ar.setRoleId(rid);
            accountRoleMapper.insert(ar);
        });
    }

    private AccountVO toVOWithoutRoles(Account a) {
        return AccountVO.builder()
                .id(a.getId())
                .username(a.getUsername())
                .email(a.getEmail())
                .phone(a.getPhone())
                .status(a.getStatus())
                .lastLoginAt(a.getLastLoginAt())
                .createTime(a.getCreateTime())
                .roles(Collections.emptyList())
                .build();
    }

    /**
     * 批量填充账号的角色列表（一次 SQL 查全部关联，避免 N+1）。
     */
    private void fillRoles(List<AccountVO> vos) {
        List<Long> accountIds = vos.stream().map(AccountVO::getId).collect(Collectors.toList());
        // 查所有 account_role 关联
        List<AccountRole> ars = accountRoleMapper.selectList(
                new LambdaQueryWrapper<AccountRole>().in(AccountRole::getAccountId, accountIds)
        );
        if (ars.isEmpty()) {
            return;
        }
        // 查所有 role
        List<Long> roleIds = ars.stream().map(AccountRole::getRoleId).distinct().collect(Collectors.toList());
        Map<Long, Role> roleMap = roleMapper.selectBatchIds(roleIds).stream()
                .collect(Collectors.toMap(Role::getId, r -> r));

        // 按 accountId 分组
        Map<Long, List<AccountVO.RoleRefVO>> rolesByAccount = ars.stream()
                .filter(ar -> roleMap.containsKey(ar.getRoleId()))
                .collect(Collectors.groupingBy(
                        AccountRole::getAccountId,
                        Collectors.mapping(ar -> {
                            Role r = roleMap.get(ar.getRoleId());
                            return AccountVO.RoleRefVO.builder()
                                    .id(r.getId()).code(r.getCode()).name(r.getName()).build();
                        }, Collectors.toList())
                ));
        vos.forEach(vo -> vo.setRoles(rolesByAccount.getOrDefault(vo.getId(), Collections.emptyList())));
    }
}
