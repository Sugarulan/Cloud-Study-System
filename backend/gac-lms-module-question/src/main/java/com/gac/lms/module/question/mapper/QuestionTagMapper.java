package com.gac.lms.module.question.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gac.lms.module.question.entity.QuestionTag;
import org.apache.ibatis.annotations.Mapper;

/**
 * 题目-标签关联 Mapper。
 *
 * @author 王茗瑾
 */
@Mapper
public interface QuestionTagMapper extends BaseMapper<QuestionTag> {
}
