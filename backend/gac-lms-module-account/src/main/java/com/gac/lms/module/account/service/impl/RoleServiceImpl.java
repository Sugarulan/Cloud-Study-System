package com.gac.lms.module.account.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gac.lms.common.exception.BusinessException;
import com.gac.lms.common.response.ErrorCode;
import com.gac.lms.common.response.PageResult;
import com.gac.lms.module.account.dto.PermissionAssignCmd;
import com.gac.lms.module.account.dto.RoleCreateCmd;
import com.gac.lms.module.account.dto.RoleUpdateCmd;
import com.gac.lms.module.account.entity.AccountRole;
import com.gac.lms.module.account.entity.Role;
import com.gac.lms.module.account.entity.RolePermission;
import com.gac.lms.module.account.mapper.AccountRoleMapper;
import com.gac.lms.module.account.mapper.RoleMapper;
import com.gac.lms.module.account.mapper.RolePermissionMapper;
import com.gac.lms.module.account.service.RoleService;
import com.gac.lms.module.account.vo.RoleVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 角色业务实现。
 *
 * @author 王茗瑾
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleMapper roleMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final AccountRoleMapper accountRoleMapper;

    @Override
    public List<RoleVO> listAll() {
        List<Role> roles = roleMapper.selectList(
                new LambdaQueryWrapper<Role>().eq(Role::getStatus, 1).orderByAsc(Role::getSort)
        );
        if (roles.isEmpty()) return Collections.emptyList();
        List<RoleVO> vos = roles.stream().map(this::toVOWithoutPerms).collect(Collectors.toList());
        fillPermissions(vos);
        return vos;
    }

    @Override
    public PageResult<RoleVO> page(String keyword, Long pageNum, Long pageSize) {
        Page<Role> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Role::getCode, keyword).or().like(Role::getName, keyword));
        }
        wrapper.orderByAsc(Role::getSort);
        Page<Role> result = roleMapper.selectPage(page, wrapper);
        List<RoleVO> records = result.getRecords().stream()
                .map(this::toVOWithoutPerms).collect(Collectors.toList());
        if (!records.isEmpty()) fillPermissions(records);
        return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), records);
    }

    @Override
    public RoleVO getById(Long id) {
        Role r = mustGet(id);
        RoleVO vo = toVOWithoutPerms(r);
        fillPermissions(List.of(vo));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RoleVO create(RoleCreateCmd cmd) {
        Long exists = roleMapper.selectCount(
                new LambdaQueryWrapper<Role>().eq(Role::getCode, cmd.getCode())
        );
        if (exists > 0) {
            throw new BusinessException(ErrorCode.DATA_ALREADY_EXISTS, "角色编码已存在");
        }
        Role r = new Role();
        r.setCode(cmd.getCode());
        r.setName(cmd.getName());
        r.setDescription(cmd.getDescription());
        r.setSort(cmd.getSort() != null ? cmd.getSort() : 0);
        r.setStatus(1);
        roleMapper.insert(r);
        log.info("[Role] created: id={} code={}", r.getId(), r.getCode());
        return getById(r.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RoleVO update(Long id, RoleUpdateCmd cmd) {
        Role r = mustGet(id);
        if (StringUtils.hasText(cmd.getName())) r.setName(cmd.getName());
        if (cmd.getDescription() != null) r.setDescription(cmd.getDescription());
        if (cmd.getSort() != null) r.setSort(cmd.getSort());
        if (cmd.getStatus() != null) r.setStatus(cmd.getStatus());
        roleMapper.updateById(r);
        log.info("[Role] updated: id={}", id);
        return getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        mustGet(id);
        // 校验是否被账号引用
        Long refs = accountRoleMapper.selectCount(
                new LambdaQueryWrapper<AccountRole>().eq(AccountRole::getRoleId, id)
        );
        if (refs > 0) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED,
                    "该角色被 " + refs + " 个账号引用，无法删除");
        }
        // 删除角色的权限关联
        rolePermissionMapper.delete(
                new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getRoleId, id)
        );
        roleMapper.deleteById(id);
        log.info("[Role] deleted: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignPermissions(Long roleId, PermissionAssignCmd cmd) {
        mustGet(roleId);
        // 清旧
        rolePermissionMapper.delete(
                new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getRoleId, roleId)
        );
        // 写新
        Set<Long> distinct = cmd.getPermissionIds().stream()
                .filter(Objects::nonNull).collect(Collectors.toSet());
        distinct.forEach(pid -> {
            RolePermission rp = new RolePermission();
            rp.setRoleId(roleId);
            rp.setPermissionId(pid);
            rolePermissionMapper.insert(rp);
        });
        log.info("[Role] assigned permissions: roleId={} permissionIds={}", roleId, distinct);
    }

    // ============== 私有方法 ==============

    private Role mustGet(Long id) {
        Role r = roleMapper.selectById(id);
        if (r == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "角色不存在");
        }
        return r;
    }

    private RoleVO toVOWithoutPerms(Role r) {
        return RoleVO.builder()
                .id(r.getId())
                .code(r.getCode())
                .name(r.getName())
                .description(r.getDescription())
                .sort(r.getSort())
                .status(r.getStatus())
                .createTime(r.getCreateTime())
                .permissionIds(Collections.emptyList())
                .build();
    }

    /**
     * 批量填充角色的权限 ID 列表（一次 SQL，避免 N+1）。
     */
    private void fillPermissions(List<RoleVO> vos) {
        List<Long> roleIds = vos.stream().map(RoleVO::getId).collect(Collectors.toList());
        List<RolePermission> rps = rolePermissionMapper.selectList(
                new LambdaQueryWrapper<RolePermission>().in(RolePermission::getRoleId, roleIds)
        );
        if (rps.isEmpty()) return;
        java.util.Map<Long, List<Long>> map = rps.stream()
                .collect(Collectors.groupingBy(
                        RolePermission::getRoleId,
                        Collectors.mapping(RolePermission::getPermissionId, Collectors.toList())
                ));
        vos.forEach(vo -> vo.setPermissionIds(map.getOrDefault(vo.getId(), Collections.emptyList())));
    }
}
