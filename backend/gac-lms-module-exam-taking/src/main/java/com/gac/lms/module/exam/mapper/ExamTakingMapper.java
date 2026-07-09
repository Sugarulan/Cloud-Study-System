package com.gac.lms.module.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gac.lms.module.exam.entity.ExamTaking;
import org.apache.ibatis.annotations.Mapper;

/**
 * 考试作答记录 Mapper。
 *
 * @author 方雨菲
 */
@Mapper
public interface ExamTakingMapper extends BaseMapper<ExamTaking> {
}
