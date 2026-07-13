package com.gac.lms.module.integration.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gac.lms.module.integration.entity.WebhookConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * Webhook 配置 Mapper。
 *
 * @author 方雨菲
 */
@Mapper
public interface WebhookConfigMapper extends BaseMapper<WebhookConfig> {
}
