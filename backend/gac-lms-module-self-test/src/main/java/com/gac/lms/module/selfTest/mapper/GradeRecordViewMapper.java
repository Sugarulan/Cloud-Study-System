package com.gac.lms.module.selfTest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gac.lms.module.selfTest.entity.GradeRecordView;
import org.apache.ibatis.annotations.Mapper;

/**
 * 成绩只读 Mapper（个人测评模块）。
 *
 * @author 方雨菲
 */
@Mapper
public interface GradeRecordViewMapper extends BaseMapper<GradeRecordView> {
}
