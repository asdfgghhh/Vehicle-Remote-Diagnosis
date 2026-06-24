package com.vrd.common.exception;

import com.vrd.common.result.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
@ConditionalOnClass(DispatcherServlet.class)
@ConditionalOnMissingClass("org.springframework.web.reactive.DispatcherHandler")
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception ex, HttpServletRequest request) {
        log.error("请求路径: {} 发生未处理异常", request.getRequestURI(), ex);
        return Result.error(ErrorCode.INTERNAL_ERROR);
    }

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBusinessException(BusinessException ex, HttpServletRequest request) {
        log.warn("业务异常 - 路径: {}, 错误: {}", request.getRequestURI(), ex.getMessage());
        return Result.error(ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败: {}", message);
        return Result.error(ErrorCode.VALIDATION_ERROR.getCode(), message);
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBindException(BindException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("参数绑定失败: {}", message);
        return Result.error(ErrorCode.BAD_REQUEST.getCode(), message);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMissingParameterException(MissingServletRequestParameterException ex) {
        String message = "缺少必需参数: " + ex.getParameterName();
        log.warn(message);
        return Result.error(ErrorCode.BAD_REQUEST.getCode(), message);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        String message = "参数类型错误: " + ex.getName();
        log.warn(message);
        return Result.error(ErrorCode.BAD_REQUEST.getCode(), message);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<Void> handleNoHandlerFoundException(NoHandlerFoundException ex) {
        log.warn("请求路径不存在: {}", ex.getRequestURL());
        return Result.error(ErrorCode.NOT_FOUND);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public Result<Void> handleMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        log.warn("不支持的请求方法: {}", ex.getMethod());
        return Result.error(ErrorCode.METHOD_NOT_ALLOWED.getCode(), "不支持的请求方法: " + ex.getMethod());
    }

    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleNullPointerException(NullPointerException ex, HttpServletRequest request) {
        log.error("空指针异常 - 请求路径: {}", request.getRequestURI(), ex);
        return Result.error(ErrorCode.INTERNAL_ERROR);
    }

    @ExceptionHandler(ArrayIndexOutOfBoundsException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleArrayIndexOutOfBoundsException(ArrayIndexOutOfBoundsException ex, HttpServletRequest request) {
        log.error("数组越界异常 - 请求路径: {}", request.getRequestURI(), ex);
        return Result.error(ErrorCode.INTERNAL_ERROR);
    }

    @ExceptionHandler(ArithmeticException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleArithmeticException(ArithmeticException ex, HttpServletRequest request) {
        log.error("算术异常 - 请求路径: {}", request.getRequestURI(), ex);
        return Result.error(ErrorCode.INTERNAL_ERROR);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleIllegalArgumentException(IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("非法参数异常 - 请求路径: {}, 错误: {}", request.getRequestURI(), ex.getMessage());
        return Result.error(ErrorCode.BAD_REQUEST.getCode(), ex.getMessage() != null ? ex.getMessage() : "参数错误");
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleIllegalStateException(IllegalStateException ex, HttpServletRequest request) {
        log.warn("非法状态异常 - 请求路径: {}, 错误: {}", request.getRequestURI(), ex.getMessage());
        return Result.error(ErrorCode.BAD_REQUEST.getCode(), ex.getMessage() != null ? ex.getMessage() : "操作非法");
    }
}