package com.gac.lms.module.person.service;

import com.gac.lms.module.person.entity.Department;
import com.gac.lms.module.person.vo.DepartmentVO;

import java.util.List;

/**
 * 部门业务接口。
 *
 * @author 王茗瑾
 */
public interface DepartmentService {

    /** 部门树（全量） */
    List<DepartmentVO> tree();

    /** 新建部门 */
    DepartmentVO create(String name, String code, Long parentId, Integer sort, Long leaderId);

    /** 更新部门 */
    DepartmentVO update(Long id, String name, String code, Long parentId, Integer sort, Long leaderId);

    /** 删除部门（无子部门且无人员） */
    void delete(Long id);

    /** 列出所有部门（扁平，用于下拉） */
    List<DepartmentVO> listAll();

    /** 获取实体（内部用） */
    Department getEntity(Long id);
}
