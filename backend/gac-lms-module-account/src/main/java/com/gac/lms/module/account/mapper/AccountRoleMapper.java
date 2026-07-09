package com.gac.lms.module.account.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gac.lms.module.account.entity.AccountRole;
import org.apache.ibatis.annotations.Mapper;

/**
 * 账号-角色关联 Mapper。
 *
 * @author 王茗瑾
 */
@Mapper
public interface AccountRoleMapper extends BaseMapper<AccountRole> {
}
