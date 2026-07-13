package com.gac.lms.module.person.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 人员出参 VO（含账号关联 + 部门列表）。
 *
 * @author 王茗瑾
 */
@Data
@Builder
@Schema(description = "人员信息")
public class PersonVO {

    @Schema(description = "人员 ID")
    private Long id;

    @Schema(description = "工号")
    private String employeeNo;

    @Schema(description = "姓名")
    private String name;

    @Schema(description = "性别：0=未知 1=男 2=女")
    private Integer gender;

    @Schema(description = "手机号")
    private String mobile;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "状态：0=离职 1=在职")
    private Integer status;

    @Schema(description = "入职日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate hiredAt;

    @Schema(description = "关联的账号 ID")
    private Long accountId;

    @Schema(description = "关联的账号用户名")
    private String accountUsername;

    @Schema(description = "归属部门列表（含主部门标记）")
    private List<DepartmentRefVO> departments;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 部门简要信息（内嵌）。
     */
    @Data
    @Builder
    @Schema(description = "部门简要")
    public static class DepartmentRefVO {
        @Schema(description = "部门 ID")
        private Long id;
        @Schema(description = "部门名称")
        private String name;
        @Schema(description = "部门编码")
        private String code;
        @Schema(description = "是否主部门")
        private Boolean isPrimary;
    }
}
