package com.gac.lms.module.account.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gac.lms.module.account.entity.Permission;
import org.apache.ibatis.annotations.Mapper;

/**
 * 权限 Mapper。
 *
 * @author 王茗瑾
 */
@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {
}
