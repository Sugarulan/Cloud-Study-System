package com.gac.lms.module.evaluation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gac.lms.module.evaluation.entity.GradeRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 成绩主表 Mapper。
 *
 * @author 方雨菲
 */
@Mapper
public interface GradeRecordMapper extends BaseMapper<GradeRecord> {
}
