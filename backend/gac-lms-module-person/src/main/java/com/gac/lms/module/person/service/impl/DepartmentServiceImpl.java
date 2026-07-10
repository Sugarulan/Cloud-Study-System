package com.gac.lms.module.person.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gac.lms.common.exception.BusinessException;
import com.gac.lms.common.response.ErrorCode;
import com.gac.lms.module.person.entity.Department;
import com.gac.lms.module.person.entity.PersonDepartment;
import com.gac.lms.module.person.mapper.DepartmentMapper;
import com.gac.lms.module.person.mapper.PersonDepartmentMapper;
import com.gac.lms.module.person.service.DepartmentService;
import com.gac.lms.module.person.vo.DepartmentVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 部门业务实现。
 *
 * <p><b>设计要点：</b></p>
 * <ul>
 *   <li>树形查询：拉全部 → 内存里组装父子关系</li>
 *   <li>删除校验：必须无子部门 + 无人员</li>
 * </ul>
 *
 * @author 王茗瑾
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentMapper departmentMapper;
    private final PersonDepartmentMapper personDepartmentMapper;

    @Override
    public List<DepartmentVO> tree() {
        // 1) 拉全部（按 sort 升序）
        List<Department> all = departmentMapper.selectList(
                new LambdaQueryWrapper<Department>().orderByAsc(Department::getSort, Department::getId)
        );
        if (all.isEmpty()) return Collections.emptyList();

        // 2) 转 VO
        Map<Long, DepartmentVO> voMap = all.stream().collect(Collectors.toMap(
                Department::getId,
                d -> DepartmentVO.builder()
                        .id(d.getId())
                        .parentId(d.getParentId())
                        .name(d.getName())
                        .code(d.getCode())
                        .sort(d.getSort())
                        .leaderId(d.getLeaderId())
                        .status(d.getStatus())
                        .children(new ArrayList<>())
                        .build()
        ));

        // 3) 组装父子关系
        List<DepartmentVO> roots = new ArrayList<>();
        for (Department d : all) {
            DepartmentVO vo = voMap.get(d.getId());
            if (d.getParentId() == null || d.getParentId() == 0) {
                roots.add(vo);
            } else {
                DepartmentVO parent = voMap.get(d.getParentId());
                if (parent != null) {
                    parent.getChildren().add(vo);
                } else {
                    // 父节点被删除，孤儿 → 当顶级
                    roots.add(vo);
                }
            }
        }
        // 顶级按 sort 排序
        roots.sort(Comparator.comparing(DepartmentVO::getSort,
                Comparator.nullsLast(Comparator.naturalOrder())));
        return roots;
    }

    @Override
    public List<DepartmentVO> listAll() {
        List<Department> all = departmentMapper.selectList(
                new LambdaQueryWrapper<Department>().orderByAsc(Department::getSort, Department::getId)
        );
        return all.stream().map(d -> DepartmentVO.builder()
                .id(d.getId())
                .parentId(d.getParentId())
                .name(d.getName())
                .code(d.getCode())
                .sort(d.getSort())
                .leaderId(d.getLeaderId())
                .status(d.getStatus())
                .build()).collect(Collectors.toList());
    }

    @Override
    public DepartmentVO create(String name, String code, Long parentId, Integer sort, Long leaderId) {
        if (!StringUtils.hasText(name)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "部门名称不能为空");
        }
        // 同 parent 下 name 唯一
        Long exists = departmentMapper.selectCount(
                new LambdaQueryWrapper<Department>()
                        .eq(Department::getName, name)
                        .eq(parentId == null ? Department::getParentId : Department::getParentId, parentId == null ? 0 : parentId)
        );
        if (exists > 0) {
            throw new BusinessException(ErrorCode.DATA_ALREADY_EXISTS, "同级部门名称已存在");
        }
        // 校验父部门存在
        if (parentId != null && parentId > 0) {
            if (departmentMapper.selectById(parentId) == null) {
                throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "父部门不存在");
            }
        }
        Department d = new Department();
        d.setName(name);
        d.setCode(code);
        d.setParentId(parentId == null ? 0L : parentId);
        d.setSort(sort != null ? sort : 0);
        d.setLeaderId(leaderId);
        d.setStatus(1);
        departmentMapper.insert(d);
        log.info("[Department] created: id={} name={}", d.getId(), d.getName());
        return DepartmentVO.builder()
                .id(d.getId()).parentId(d.getParentId()).name(d.getName())
                .code(d.getCode()).sort(d.getSort()).leaderId(d.getLeaderId())
                .status(d.getStatus()).build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DepartmentVO update(Long id, String name, String code, Long parentId, Integer sort, Long leaderId) {
        Department d = mustGet(id);
        if (StringUtils.hasText(name)) d.setName(name);
        if (code != null) d.setCode(code);
        if (parentId != null) {
            if (parentId.equals(id)) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "父部门不能是自己");
            }
            if (parentId > 0 && departmentMapper.selectById(parentId) == null) {
                throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "父部门不存在");
            }
            // TODO W3: 校验不能把部门移到自己的子部门下（避免循环）
            d.setParentId(parentId);
        }
        if (sort != null) d.setSort(sort);
        if (leaderId != null) d.setLeaderId(leaderId);
        departmentMapper.updateById(d);
        log.info("[Department] updated: id={}", id);
        return DepartmentVO.builder()
                .id(d.getId()).parentId(d.getParentId()).name(d.getName())
                .code(d.getCode()).sort(d.getSort()).leaderId(d.getLeaderId())
                .status(d.getStatus()).build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        mustGet(id);
        // 校验无子部门
        Long childCount = departmentMapper.selectCount(
                new LambdaQueryWrapper<Department>().eq(Department::getParentId, id)
        );
        if (childCount > 0) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED,
                    "该部门下有 " + childCount + " 个子部门，无法删除");
        }
        // 校验无人员
        Long personCount = personDepartmentMapper.selectCount(
                new LambdaQueryWrapper<PersonDepartment>().eq(PersonDepartment::getDepartmentId, id)
        );
        if (personCount > 0) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED,
                    "该部门下有 " + personCount + " 个人员，无法删除");
        }
        departmentMapper.deleteById(id);
        log.info("[Department] deleted: id={}", id);
    }

    @Override
    public Department getEntity(Long id) {
        return mustGet(id);
    }

    private Department mustGet(Long id) {
        Department d = departmentMapper.selectById(id);
        if (d == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "部门不存在");
        }
        return d;
    }
}
