package com.gac.lms.module.person.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

/**
 * 更新人员请求体。
 *
 * @author 王茗瑾
 */
@Data
@Schema(description = "更新人员请求体")
public class PersonUpdateCmd {

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

    @Schema(description = "工号（业务主键，谨慎修改）")
    private String employeeNo;
}
