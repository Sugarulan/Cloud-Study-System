package com.gac.lms.common.exception;

import com.gac.lms.common.response.ErrorCode;
import com.gac.lms.common.response.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器。
 *
 * <p>将异常统一包装为 {@link Result} 格式返回，便于前端解析。</p>
 *
 * <p><b>注意</b>：本类需要被 spring 扫描到，因此由启动模块引入。</p>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 业务异常 */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusiness(BusinessException ex, HttpServletRequest req) {
        log.warn("[BusinessException] path={} code={} msg={}", req.getRequestURI(), ex.getCode(), ex.getMessage());
        return ResponseEntity.ok(Result.fail(ex.getCode(), ex.getMessage()));
    }

    /** @Valid 校验异常 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleValid(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .orElse(ErrorCode.BAD_REQUEST.getMessage());
        return ResponseEntity.ok(Result.fail(ErrorCode.BAD_REQUEST.getCode(), msg));
    }

    /** 表单绑定异常 */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<Result<Void>> handleBind(BindException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .orElse(ErrorCode.BAD_REQUEST.getMessage());
        return ResponseEntity.ok(Result.fail(ErrorCode.BAD_REQUEST.getCode(), msg));
    }

    /**
     * 缺少必填请求头（如 {@code X-User-Id}） → 401 业务码。
     */
    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<Result<Void>> handleMissingHeader(MissingRequestHeaderException ex) {
        log.warn("[MissingRequestHeaderException] header={}", ex.getHeaderName());
        return ResponseEntity.ok(Result.fail(ErrorCode.UNAUTHORIZED.getCode(),
                "缺少必填请求头: " + ex.getHeaderName()));
    }

    /**
     * 缺少必填请求参数（如 {@code ?examId=xxx}） → 400 业务码。
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Result<Void>> handleMissingParam(MissingServletRequestParameterException ex) {
        log.warn("[MissingServletRequestParameterException] param={}", ex.getParameterName());
        return ResponseEntity.ok(Result.fail(ErrorCode.BAD_REQUEST.getCode(),
                "缺少必填参数: " + ex.getParameterName()));
    }

    /** 兜底异常 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleUnknown(Exception ex, HttpServletRequest req) {
        log.error("[UnknownException] path={}", req.getRequestURI(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.fail(ErrorCode.SYSTEM_ERROR));
    }
}
