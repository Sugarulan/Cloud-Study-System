package com.gac.lms.common.exception;

import com.gac.lms.common.response.ErrorCode;
import lombok.Getter;

/**
 * 业务异常，所有受检的业务异常都应抛出本类。
 *
 * <p>全局异常处理器会捕获本类并包装为统一的 {@code Result} 返回。</p>
 */
@Getter
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final int code;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
