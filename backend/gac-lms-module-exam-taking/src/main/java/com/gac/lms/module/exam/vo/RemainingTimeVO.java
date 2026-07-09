package com.gac.lms.module.exam.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 剩余时间。
 *
 * @author 方雨菲
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "考试剩余时间")
public class RemainingTimeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 剩余秒数（0 表示已到结束时间） */
    private Long remainingSeconds;

    /** 是否已超时 */
    private Boolean overtime;

    /** 服务端当前时间（用于前端校时） */
    private Long serverTime;
}
