package com.gac.lms.module.grade.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gac.lms.module.grade.entity.GradeRecord;
import com.gac.lms.module.grade.vo.GradeStatisticsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 成绩主表 Mapper。
 *
 * <p>复用 BaseMapper 提供 CRUD，自定义统计查询。</p>
 *
 * @author 方雨菲
 */
@Mapper
public interface GradeRecordMapper extends BaseMapper<GradeRecord> {

    /**
     * 统计某场考试的均分与通过率。
     */
    GradeStatisticsVO statistics(@Param("examId") Long examId);

    /**
     * 分数段分布。
     */
    List<GradeStatisticsVO.ScoreBucket> distribution(@Param("examId") Long examId);
}
