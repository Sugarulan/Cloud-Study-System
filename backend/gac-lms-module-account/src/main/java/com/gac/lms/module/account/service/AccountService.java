package com.gac.lms.module.account.service;

import com.gac.lms.common.response.PageResult;
import com.gac.lms.module.account.dto.AccountCreateCmd;
import com.gac.lms.module.account.dto.AccountQuery;
import com.gac.lms.module.account.dto.AccountUpdateCmd;
import com.gac.lms.module.account.dto.PasswordChangeCmd;
import com.gac.lms.module.account.dto.PasswordResetCmd;
import com.gac.lms.module.account.dto.RoleAssignCmd;
import com.gac.lms.module.account.vo.AccountVO;

/**
 * 账号业务接口。
 *
 * @author 王茗瑾
 */
public interface AccountService {

    /** 分页查询 */
    PageResult<AccountVO> page(AccountQuery query);

    /** 详情 */
    AccountVO getById(Long id);

    /** 创建账号（BCrypt 加密密码，可选分配角色） */
    AccountVO create(AccountCreateCmd cmd);

    /** 更新（邮箱/手机/状态） */
    AccountVO update(Long id, AccountUpdateCmd cmd);

    /** 启用 */
    void enable(Long id);

    /** 停用 */
    void disable(Long id);

    /** 管理员重置密码 */
    void resetPassword(Long id, PasswordResetCmd cmd);

    /** 修改自己的密码（校验旧密码） */
    void changePassword(Long id, PasswordChangeCmd cmd);

    /** 查询账号的角色列表 */
    java.util.List<AccountVO.RoleRefVO> listRoles(Long accountId);

    /** 替换式分配角色，返回成功分配的角色数量 */
    int assignRoles(Long accountId, RoleAssignCmd cmd);
}
