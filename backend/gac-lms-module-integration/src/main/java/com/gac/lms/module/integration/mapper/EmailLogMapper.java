package com.gac.lms.module.integration.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gac.lms.module.integration.entity.EmailLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 邮件日志 Mapper。
 *
 * @author 方雨菲
 */
@Mapper
public interface EmailLogMapper extends BaseMapper<EmailLog> {
}
