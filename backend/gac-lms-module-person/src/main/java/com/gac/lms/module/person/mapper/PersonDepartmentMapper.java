package com.gac.lms.module.person.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gac.lms.module.person.entity.PersonDepartment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 人员-部门关联 Mapper。
 *
 * @author 王茗瑾
 */
@Mapper
public interface PersonDepartmentMapper extends BaseMapper<PersonDepartment> {
}
