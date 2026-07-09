package com.gac.lms.module.person.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gac.lms.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 人员实体（对应表 person）。
 *
 * <p>对应 schema.sql §二.3.3.2 人员表。
 * 1:1 关联 account（可空，人员可以无账号）。</p>
 *
 * @author 王茗瑾
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("person")
public class Person extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 账号 ID（1:1，可空） */
    private Long accountId;

    /** 工号（唯一） */
    private String employeeNo;

    /** 姓名 */
    private String name;

    /** 性别：0=未知，1=男，2=女 */
    private Integer gender;

    /** 手机号 */
    private String mobile;

    /** 邮箱 */
    private String email;

    /** 状态：0=离职，1=在职 */
    private Integer status;

    /** 入职日期 */
    private LocalDate hiredAt;
}
