package com.gac.lms.module.knowledge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 审核请求。
 *
 * @author 方雨菲
 */
@Data
@Schema(description = "审核请求")
public class ReviewRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 驳回原因（仅 reject 时使用） */
    private String reason;
}
