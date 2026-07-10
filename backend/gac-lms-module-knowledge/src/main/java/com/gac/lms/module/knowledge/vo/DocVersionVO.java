package com.gac.lms.module.knowledge.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文档版本摘要（不含 content，避免响应过大）。
 *
 * @author 方雨菲
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocVersionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long docId;
    private Integer version;
    private String title;
    private String changeLog;
    private Long operatorId;
    private LocalDateTime createTime;
}
