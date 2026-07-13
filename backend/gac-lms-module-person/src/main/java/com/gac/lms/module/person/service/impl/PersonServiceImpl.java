package com.gac.lms.module.person.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gac.lms.common.exception.BusinessException;
import com.gac.lms.common.response.ErrorCode;
import com.gac.lms.common.response.PageResult;
import com.gac.lms.module.account.dto.AccountCreateCmd;
import com.gac.lms.module.account.service.AccountService;
import com.gac.lms.module.account.vo.AccountVO;
import com.gac.lms.module.person.dto.PersonCreateCmd;
import com.gac.lms.module.person.dto.PersonQuery;
import com.gac.lms.module.person.dto.PersonUpdateCmd;
import com.gac.lms.module.person.entity.Department;
import com.gac.lms.module.person.entity.Person;
import com.gac.lms.module.person.entity.PersonDepartment;
import com.gac.lms.module.person.mapper.DepartmentMapper;
import com.gac.lms.module.person.mapper.PersonDepartmentMapper;
import com.gac.lms.module.person.mapper.PersonMapper;
import com.gac.lms.module.person.service.PersonService;
import com.gac.lms.module.person.vo.PersonVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 人员业务实现。
 *
 * <p><b>设计要点：</b></p>
 * <ul>
 *   <li>创建人员时可选调用 AccountService 创建账号（依赖 account 模块）</li>
 *   <li>部门关联用替换式写入（先清旧再写新）</li>
 *   <li>主部门标记：primaryDepartmentId 必须是 departmentIds 之一</li>
 * </ul>
 *
 * @author 王茗瑾
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PersonServiceImpl implements PersonService {

    private final PersonMapper personMapper;
    private final DepartmentMapper departmentMapper;
    private final PersonDepartmentMapper personDepartmentMapper;
    private final AccountService accountService;  // person → account 单向依赖

    // ============== 查询 ==============

    @Override
    public PageResult<PersonVO> page(PersonQuery query) {
        Page<Person> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<Person> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(Person::getEmployeeNo, query.getKeyword())
                    .or().like(Person::getName, query.getKeyword())
                    .or().like(Person::getMobile, query.getKeyword())
                    .or().like(Person::getEmail, query.getKeyword()));
        }
        if (query.getStatus() != null) {
            wrapper.eq(Person::getStatus, query.getStatus());
        }
        wrapper.orderByDesc(Person::getCreateTime);

        Page<Person> result = personMapper.selectPage(page, wrapper);
        List<PersonVO> records = result.getRecords().stream()
                .map(this::toBaseVO).collect(Collectors.toList());

        if (!records.isEmpty()) {
            // 部门过滤
            if (query.getDepartmentId() != null) {
                List<Long> personIdsInDept = getPersonIdsInDepartment(query.getDepartmentId());
                records = records.stream()
                        .filter(p -> personIdsInDept.contains(p.getId()))
                        .collect(Collectors.toList());
            }
            fillDepartments(records);
        }
        return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), records);
    }

    @Override
    public PersonVO getById(Long id) {
        Person p = mustGet(id);
        PersonVO vo = toFullVO(p);
        fillDepartments(List.of(vo));
        return vo;
    }

    @Override
    public List<PersonVO.DepartmentRefVO> listDepartments(Long personId) {
        mustGet(personId);
        List<PersonDepartment> pds = personDepartmentMapper.selectList(
                new LambdaQueryWrapper<PersonDepartment>().eq(PersonDepartment::getPersonId, personId)
        );
        if (pds.isEmpty()) return Collections.emptyList();
        List<Long> deptIds = pds.stream().map(PersonDepartment::getDepartmentId).collect(Collectors.toList());
        Map<Long, Department> deptMap = departmentMapper.selectBatchIds(deptIds).stream()
                .collect(Collectors.toMap(Department::getId, d -> d));
        return pds.stream().map(pd -> {
            Department d = deptMap.get(pd.getDepartmentId());
            if (d == null) return null;
            return PersonVO.DepartmentRefVO.builder()
                    .id(d.getId())
                    .name(d.getName())
                    .code(d.getCode())
                    .isPrimary(pd.getIsPrimary() != null && pd.getIsPrimary() == 1)
                    .build();
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    // ============== 写入 ==============

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PersonVO create(PersonCreateCmd cmd) {
        // 1) 校验工号唯一
        Long exists = personMapper.selectCount(
                new LambdaQueryWrapper<Person>().eq(Person::getEmployeeNo, cmd.getEmployeeNo())
        );
        if (exists > 0) {
            throw new BusinessException(ErrorCode.DATA_ALREADY_EXISTS, "工号已存在");
        }
        // 2) 可选创建账号
        Long accountId = null;
        String accountUsername = null;
        if (Boolean.TRUE.equals(cmd.getCreateAccount())) {
            if (!StringUtils.hasText(cmd.getUsername()) || !StringUtils.hasText(cmd.getPassword())) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "创建账号时必须填写 username/password");
            }
            AccountCreateCmd accountCmd = new AccountCreateCmd();
            accountCmd.setUsername(cmd.getUsername());
            accountCmd.setPassword(cmd.getPassword());
            accountCmd.setEmail(cmd.getEmail());
            accountCmd.setRoleIds(cmd.getRoleIds());
            AccountVO accountVO = accountService.create(accountCmd);
            accountId = accountVO.getId();
            accountUsername = accountVO.getUsername();
        }

        // 3) 校验主部门
        if (cmd.getPrimaryDepartmentId() != null && cmd.getDepartmentIds() != null
                && !cmd.getDepartmentIds().contains(cmd.getPrimaryDepartmentId())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "主部门必须是部门列表之一");
        }

        // 4) 插入人员
        Person p = new Person();
        p.setEmployeeNo(cmd.getEmployeeNo());
        p.setName(cmd.getName());
        p.setGender(cmd.getGender());
        p.setMobile(cmd.getMobile());
        p.setEmail(cmd.getEmail());
        p.setStatus(cmd.getStatus() != null ? cmd.getStatus() : 1);
        p.setHiredAt(cmd.getHiredAt());
        p.setAccountId(accountId);
        personMapper.insert(p);
        log.info("[Person] created: id={} employeeNo={} accountId={}", p.getId(), p.getEmployeeNo(), accountId);

        // 5) 关联部门
        if (cmd.getDepartmentIds() != null && !cmd.getDepartmentIds().isEmpty()) {
            assignDepartmentsInternal(p.getId(), cmd.getDepartmentIds(), cmd.getPrimaryDepartmentId());
        }

        PersonVO vo = toFullVO(p);
        vo.setAccountUsername(accountUsername);
        fillDepartments(List.of(vo));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PersonVO update(Long id, PersonUpdateCmd cmd) {
        Person p = mustGet(id);
        if (StringUtils.hasText(cmd.getName())) p.setName(cmd.getName());
        if (cmd.getGender() != null) p.setGender(cmd.getGender());
        if (cmd.getMobile() != null) p.setMobile(cmd.getMobile());
        if (cmd.getEmail() != null) p.setEmail(cmd.getEmail());
        if (cmd.getHiredAt() != null) p.setHiredAt(cmd.getHiredAt());
        if (cmd.getStatus() != null) p.setStatus(cmd.getStatus());
        if (StringUtils.hasText(cmd.getEmployeeNo())) {
            // 工号变更时校验唯一
            if (!cmd.getEmployeeNo().equals(p.getEmployeeNo())) {
                Long exists = personMapper.selectCount(
                        new LambdaQueryWrapper<Person>().eq(Person::getEmployeeNo, cmd.getEmployeeNo()));
                if (exists > 0) {
                    throw new BusinessException(ErrorCode.DATA_ALREADY_EXISTS, "工号已存在");
                }
                p.setEmployeeNo(cmd.getEmployeeNo());
            }
        }
        personMapper.updateById(p);
        log.info("[Person] updated: id={}", id);
        return getById(id);
    }

    @Override
    public void delete(Long id) {
        mustGet(id);
        personMapper.deleteById(id);
        // 同步清理关联（中间表无逻辑删除字段，物理删除）
        personDepartmentMapper.delete(
                new LambdaQueryWrapper<PersonDepartment>().eq(PersonDepartment::getPersonId, id)
        );
        log.info("[Person] deleted: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignDepartments(Long personId, List<Long> departmentIds, Long primaryDepartmentId) {
        mustGet(personId);
        assignDepartmentsInternal(personId, departmentIds, primaryDepartmentId);
        log.info("[Person] assigned departments: personId={} count={} primary={}",
                personId, departmentIds == null ? 0 : departmentIds.size(), primaryDepartmentId);
    }

    // ============== 私有方法 ==============

    private void assignDepartmentsInternal(Long personId, List<Long> departmentIds, Long primaryDepartmentId) {
        // 清旧
        personDepartmentMapper.delete(
                new LambdaQueryWrapper<PersonDepartment>().eq(PersonDepartment::getPersonId, personId)
        );
        if (departmentIds == null || departmentIds.isEmpty()) return;

        // 校验主部门
        Long primary = primaryDepartmentId;
        if (primary != null && !departmentIds.contains(primary)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "主部门必须是部门列表之一");
        }

        // 校验部门都存在
        List<Department> depts = departmentMapper.selectBatchIds(departmentIds);
        if (depts.size() != departmentIds.stream().distinct().count()) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "部分部门 ID 不存在");
        }

        // 写新
        Set<Long> distinct = departmentIds.stream().filter(Objects::nonNull).collect(Collectors.toSet());
        for (Long deptId : distinct) {
            PersonDepartment pd = new PersonDepartment();
            pd.setPersonId(personId);
            pd.setDepartmentId(deptId);
            pd.setIsPrimary(deptId.equals(primary) ? 1 : 0);
            personDepartmentMapper.insert(pd);
        }
    }

    private Person mustGet(Long id) {
        Person p = personMapper.selectById(id);
        if (p == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "人员不存在");
        }
        return p;
    }

    private PersonVO toBaseVO(Person p) {
        return toFullVO(p);
    }

    private PersonVO toFullVO(Person p) {
        PersonVO.PersonVOBuilder b = PersonVO.builder()
                .id(p.getId())
                .employeeNo(p.getEmployeeNo())
                .name(p.getName())
                .gender(p.getGender())
                .mobile(p.getMobile())
                .email(p.getEmail())
                .status(p.getStatus())
                .hiredAt(p.getHiredAt())
                .accountId(p.getAccountId())
                .departments(Collections.emptyList())
                .createTime(p.getCreateTime());
        return b.build();
    }

    /**
     * 批量填充人员的部门列表。
     */
    private void fillDepartments(List<PersonVO> vos) {
        if (vos.isEmpty()) return;
        List<Long> personIds = vos.stream().map(PersonVO::getId).collect(Collectors.toList());
        List<PersonDepartment> pds = personDepartmentMapper.selectList(
                new LambdaQueryWrapper<PersonDepartment>().in(PersonDepartment::getPersonId, personIds)
        );
        if (pds.isEmpty()) return;
        List<Long> deptIds = pds.stream().map(PersonDepartment::getDepartmentId)
                .distinct().collect(Collectors.toList());
        Map<Long, Department> deptMap = departmentMapper.selectBatchIds(deptIds).stream()
                .collect(Collectors.toMap(Department::getId, d -> d));

        Map<Long, List<PersonVO.DepartmentRefVO>> grouped = pds.stream()
                .filter(pd -> deptMap.containsKey(pd.getDepartmentId()))
                .collect(Collectors.groupingBy(
                        PersonDepartment::getPersonId,
                        Collectors.mapping(pd -> {
                            Department d = deptMap.get(pd.getDepartmentId());
                            return PersonVO.DepartmentRefVO.builder()
                                    .id(d.getId()).name(d.getName()).code(d.getCode())
                                    .isPrimary(pd.getIsPrimary() != null && pd.getIsPrimary() == 1)
                                    .build();
                        }, Collectors.toList())));
        vos.forEach(vo -> vo.setDepartments(grouped.getOrDefault(vo.getId(), Collections.emptyList())));
    }

    /**
     * 查某个部门下（含子部门）的所有人员 ID。
     */
    private List<Long> getPersonIdsInDepartment(Long departmentId) {
        // 简化：只查本部门，不递归子部门。W3 优化。
        List<PersonDepartment> pds = personDepartmentMapper.selectList(
                new LambdaQueryWrapper<PersonDepartment>().eq(PersonDepartment::getDepartmentId, departmentId)
        );
        if (pds.isEmpty()) return new ArrayList<>();
        return pds.stream().map(PersonDepartment::getPersonId).collect(Collectors.toList());
    }

    // 防止 import 报错（Map 在 groupBy 用到）
    @SuppressWarnings("unused")
    private Map<String, String> unused() { return Collections.emptyMap(); }
    // 防止 import 报错
    @SuppressWarnings("unused")
    private LocalDateTime unusedTime() { return LocalDateTime.now(); }
}
