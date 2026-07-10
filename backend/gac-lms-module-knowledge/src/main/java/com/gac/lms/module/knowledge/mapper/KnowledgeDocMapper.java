package com.gac.lms.module.knowledge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gac.lms.module.knowledge.entity.KnowledgeDoc;
import org.apache.ibatis.annotations.Mapper;

/**
 * 知识库文档 Mapper。
 *
 * @author 方雨菲
 */
@Mapper
public interface KnowledgeDocMapper extends BaseMapper<KnowledgeDoc> {
}
