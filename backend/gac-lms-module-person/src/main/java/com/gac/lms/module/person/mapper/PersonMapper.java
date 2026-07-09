package com.gac.lms.module.person.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gac.lms.module.person.entity.Person;
import org.apache.ibatis.annotations.Mapper;

/**
 * 人员 Mapper。
 *
 * @author 王茗瑾
 */
@Mapper
public interface PersonMapper extends BaseMapper<Person> {
}
