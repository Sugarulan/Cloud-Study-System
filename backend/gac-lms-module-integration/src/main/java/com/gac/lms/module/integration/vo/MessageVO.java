package com.gac.lms.module.integration.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 站内信 VO。
 *
 * @author 方雨菲
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long userId;
    private String type;
    private String title;
    private String content;
    private Integer isRead;
    private LocalDateTime readTime;
    private LocalDateTime createTime;
}
