package com.gac.lms.module.knowledge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gac.lms.module.knowledge.entity.KnowledgeDocVersion;
import org.apache.ibatis.annotations.Mapper;

/**
 * 知识库文档版本 Mapper。
 *
 * @author 方雨菲
 */
@Mapper
public interface KnowledgeDocVersionMapper extends BaseMapper<KnowledgeDocVersion> {
}
