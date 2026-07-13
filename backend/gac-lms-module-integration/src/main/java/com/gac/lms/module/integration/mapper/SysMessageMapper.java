package com.gac.lms.module.integration.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gac.lms.module.integration.entity.SysMessage;
import org.apache.ibatis.annotations.Mapper;

/**
 * 站内信 Mapper。
 *
 * @author 方雨菲
 */
@Mapper
public interface SysMessageMapper extends BaseMapper<SysMessage> {
}
