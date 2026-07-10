package com.gac.lms.module.knowledge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gac.lms.module.knowledge.entity.KnowledgeCategory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 知识库分类 Mapper。
 *
 * @author 方雨菲
 */
@Mapper
public interface KnowledgeCategoryMapper extends BaseMapper<KnowledgeCategory> {
}
