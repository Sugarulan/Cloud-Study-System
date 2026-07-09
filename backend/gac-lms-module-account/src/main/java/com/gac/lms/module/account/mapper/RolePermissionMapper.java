package com.gac.lms.module.account.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gac.lms.module.account.entity.RolePermission;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色-权限关联 Mapper。
 *
 * @author 王茗瑾
 */
@Mapper
public interface RolePermissionMapper extends BaseMapper<RolePermission> {
}
