package com.gac.lms.module.account.service;

import com.gac.lms.common.response.PageResult;
import com.gac.lms.module.account.dto.PermissionAssignCmd;
import com.gac.lms.module.account.dto.RoleCreateCmd;
import com.gac.lms.module.account.dto.RoleUpdateCmd;
import com.gac.lms.module.account.vo.RoleVO;

import java.util.List;

/**
 * 角色业务接口。
 *
 * @author 王茗瑾
 */
public interface RoleService {

    /** 列表（不分页，简单 List） */
    List<RoleVO> listAll();

    /** 分页 + 关键字 */
    PageResult<RoleVO> page(String keyword, Long pageNum, Long pageSize);

    /** 详情 */
    RoleVO getById(Long id);

    /** 创建 */
    RoleVO create(RoleCreateCmd cmd);

    /** 更新（不允许改 code） */
    RoleVO update(Long id, RoleUpdateCmd cmd);

    /** 删除（带校验：被账号引用则禁止） */
    void delete(Long id);

    /** 替换式分配权限 */
    void assignPermissions(Long roleId, PermissionAssignCmd cmd);
}
