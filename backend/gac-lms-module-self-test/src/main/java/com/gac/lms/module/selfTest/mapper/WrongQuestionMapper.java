package com.gac.lms.module.selfTest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gac.lms.module.selfTest.entity.WrongQuestion;
import org.apache.ibatis.annotations.Mapper;

/**
 * 错题本 Mapper。
 *
 * @author 方雨菲
 */
@Mapper
public interface WrongQuestionMapper extends BaseMapper<WrongQuestion> {
}
