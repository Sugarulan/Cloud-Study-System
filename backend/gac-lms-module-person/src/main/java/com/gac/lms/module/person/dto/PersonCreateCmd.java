package com.gac.lms.module.person.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 创建人员请求体。
 *
 * <p>关键字段：</p>
 * <ul>
 *   <li>{@code createAccount=true} 时需填 {@code username/password/roleIds}</li>
 *   <li>{@code departmentIds} 可设置人员的归属部门（至少 1 个）</li>
 *   <li>{@code primaryDepartmentId} 必须是 departmentIds 之一</li>
 * </ul>
 *
 * @author 王茗瑾
 */
@Data
@Schema(description = "创建人员请求体")
public class PersonCreateCmd {

    @Schema(description = "工号（业务主键）", example = "E1002")
    private String employeeNo;

    @Schema(description = "姓名")
    private String name;

    @Schema(description = "性别：0=未知 1=男 2=女")
    private Integer gender;

    @Schema(description = "手机号")
    private String mobile;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "入职日期")
    private LocalDate hiredAt;

    @Schema(description = "状态：0=离职 1=在职")
    private Integer status;

    @Schema(description = "是否同时创建账号")
    private Boolean createAccount;

    /** 当 createAccount=true 时必填 */
    @Schema(description = "登录名（createAccount=true 时必填）")
    private String username;

    /** 当 createAccount=true 时必填 */
    @Schema(description = "初始密码（createAccount=true 时必填）")
    private String password;

    @Schema(description = "账号角色 ID 列表")
    private List<Long> roleIds;

    @Schema(description = "部门 ID 列表（创建后通过部门关联接口分配也可）")
    private List<Long> departmentIds;

    @Schema(description = "主部门 ID（必须是 departmentIds 之一）")
    private Long primaryDepartmentId;
}
