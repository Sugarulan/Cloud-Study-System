package com.gac.lms.common.response;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应包装。
 *
 * <pre>
 * {
 *   "code": 0,
 *   "message": "success",
 *   "data": ...,
 *   "traceId": "abc123"
 * }
 * </pre>
 *
 * @param <T> 业务数据类型
 * @author 方雨菲
 */
@Data
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 业务码，0=成功，非 0=失败 */
    private int code;

    /** 提示信息 */
    private String message;

    /** 业务数据 */
    private T data;

    /** 链路追踪 ID，便于排查问题 */
    private String traceId;

    public static <T> Result<T> ok() {
        return ok(null);
    }

    public static <T> Result<T> ok(T data) {
        Result<T> r = new Result<>();
        r.setCode(ErrorCode.SUCCESS.getCode());
        r.setMessage(ErrorCode.SUCCESS.getMessage());
        r.setData(data);
        return r;
    }

    public static <T> Result<T> fail(ErrorCode errorCode) {
        return fail(errorCode, errorCode.getMessage());
    }

    public static <T> Result<T> fail(ErrorCode errorCode, String message) {
        Result<T> r = new Result<>();
        r.setCode(errorCode.getCode());
        r.setMessage(message);
        return r;
    }

    public boolean isSuccess() {
        return this.code == ErrorCode.SUCCESS.getCode();
    }
}
