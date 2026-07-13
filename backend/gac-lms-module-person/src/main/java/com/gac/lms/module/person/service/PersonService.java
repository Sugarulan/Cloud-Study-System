package com.gac.lms.module.person.service;

import com.gac.lms.common.response.PageResult;
import com.gac.lms.module.person.dto.PersonCreateCmd;
import com.gac.lms.module.person.dto.PersonQuery;
import com.gac.lms.module.person.dto.PersonUpdateCmd;
import com.gac.lms.module.person.vo.PersonVO;

import java.util.List;

/**
 * 人员业务接口。
 *
 * @author 王茗瑾
 */
public interface PersonService {

    /** 分页查询 */
    PageResult<PersonVO> page(PersonQuery query);

    /** 详情（含账号关联 + 部门列表） */
    PersonVO getById(Long id);

    /** 创建人员（可选同时创建账号） */
    PersonVO create(PersonCreateCmd cmd);

    /** 更新人员 */
    PersonVO update(Long id, PersonUpdateCmd cmd);

    /** 离职（软删） */
    void delete(Long id);

    /** 给人员分配部门（替换式，含主部门） */
    void assignDepartments(Long personId, List<Long> departmentIds, Long primaryDepartmentId);

    /** 查询人员的部门列表 */
    List<PersonVO.DepartmentRefVO> listDepartments(Long personId);
}
