package com.gac.lms.module.integration.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 邮件发送结果。
 *
 * @author 方雨菲
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailSendResultVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 日志 ID */
    private Long logId;

    /** 发送成功数量 */
    private Integer successCount;

    /** 发送失败数量 */
    private Integer failedCount;

    /** 失败详情 */
    private List<String> failedRecipients;

    /** 模板代码 */
    private String templateCode;
}
