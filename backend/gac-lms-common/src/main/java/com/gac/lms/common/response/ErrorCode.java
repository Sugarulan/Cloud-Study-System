package com.gac.lms.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 错误码定义。
 *
 * <ul>
 *   <li>0     成功</li>
 *   <li>1xxxx 通用错误（参数、权限、系统）</li>
 *   <li>2xxxx 业务错误</li>
 *   <li>3xxxx 第三方错误（AI、邮件）</li>
 * </ul>
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    // ===== 通用 =====
    SUCCESS(0, "success"),
    BAD_REQUEST(10001, "请求参数错误"),
    UNAUTHORIZED(10002, "未登录或登录已过期"),
    FORBIDDEN(10003, "无权限访问"),
    NOT_FOUND(10004, "资源不存在"),
    METHOD_NOT_ALLOWED(10005, "请求方法不被允许"),
    SYSTEM_ERROR(10999, "系统异常，请稍后重试"),

    // ===== 业务通用 =====
    DATA_NOT_FOUND(20001, "数据不存在"),
    DATA_ALREADY_EXISTS(20002, "数据已存在"),
    DATA_VERSION_CONFLICT(20003, "数据版本冲突，请刷新后重试"),
    OPERATION_NOT_ALLOWED(20004, "当前状态不允许该操作"),

    // ===== 在线作答 =====
    EXAM_NOT_STARTED(21001, "考试尚未开始"),
    EXAM_ENDED(21002, "考试已结束"),
    EXAM_NOT_IN_USER_SCOPE(21003, "您不在本次考试参考范围内"),
    ANSWER_SAVE_CONFLICT(21004, "答题版本冲突，请刷新后重试"),

    // ===== 评卷 =====
    EVALUATION_AI_FAIL(22001, "AI 评卷失败，已转人工评卷"),
    EVALUATION_PUBLISHED(22002, "成绩已发布，不可重复发布"),

    // ===== AI =====
    AI_PROVIDER_NOT_FOUND(30001, "AI Provider 未配置"),
    AI_INVOKE_TIMEOUT(30002, "AI 调用超时"),
    AI_INVOKE_FAIL(30003, "AI 调用失败");

    private final int code;
    private final String message;
}
