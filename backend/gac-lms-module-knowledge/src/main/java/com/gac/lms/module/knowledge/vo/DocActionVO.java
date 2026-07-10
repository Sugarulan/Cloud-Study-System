package com.gac.lms.module.knowledge.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文档状态变更结果（提交/审核/发布/归档）。
 *
 * @author 方雨菲
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "文档状态变更结果")
public class DocActionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long docId;
    private Integer status;
    private String statusLabel;
    private Integer version;
    private LocalDateTime actionTime;
    private Long operatorId;
    private String message;
}
