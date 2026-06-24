package com.vrd.common.exception;

/**
 * 错误码枚举类
 * 统一管理系统所有错误码和错误消息
 */
public enum ErrorCode {

    SUCCESS(200, "success"),
    
    // 客户端错误 (4xx)
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权，请重新登录"),
    FORBIDDEN(403, "拒绝访问"),
    NOT_FOUND(404, "请求的资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),
    UNSUPPORTED_MEDIA_TYPE(415, "不支持的媒体类型"),
    TOO_MANY_REQUESTS(429, "请求过于频繁，请稍后重试"),
    
    // 服务器错误 (5xx)
    INTERNAL_ERROR(500, "系统繁忙，请稍后重试"),
    BAD_GATEWAY(502, "服务暂时不可用，请稍后重试"),
    SERVICE_UNAVAILABLE(503, "服务暂时不可用，请稍后重试"),
    GATEWAY_TIMEOUT(504, "服务暂时不可用，请稍后重试"),
    
    // 业务错误 (1000+)
    BUSINESS_ERROR(1000, "业务处理失败"),
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_PASSWORD_ERROR(1002, "用户名或密码错误"),
    USER_DISABLED(1003, "用户已被禁用"),
    USER_ALREADY_EXISTS(1004, "用户名已存在"),
    VEHICLE_NOT_FOUND(1101, "车辆不存在"),
    ECU_NOT_FOUND(1102, "ECU不存在"),
    DBC_NOT_FOUND(1201, "DBC文件不存在"),
    LOG_NOT_FOUND(1301, "日志不存在"),
    SIGNAL_NOT_FOUND(1401, "信号数据不存在"),
    FILE_UPLOAD_ERROR(1501, "文件上传失败"),
    FILE_SIZE_EXCEEDED(1502, "文件大小超过限制"),
    VALIDATION_ERROR(1601, "数据校验失败"),
    TOKEN_INVALID(1701, "令牌无效"),
    TOKEN_EXPIRED(1702, "令牌已过期"),
    PERMISSION_DENIED(1801, "权限不足"),
    DATA_INTEGRITY_ERROR(1901, "数据完整性错误");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    /**
     * 根据状态码获取对应的错误码枚举
     * @param statusCode HTTP状态码
     * @return 错误码枚举
     */
    public static ErrorCode fromStatusCode(int statusCode) {
        for (ErrorCode errorCode : values()) {
            if (errorCode.getCode() == statusCode) {
                return errorCode;
            }
        }
        if (statusCode >= 500) {
            return INTERNAL_ERROR;
        }
        if (statusCode >= 400) {
            return BAD_REQUEST;
        }
        return SUCCESS;
    }

    /**
     * 创建业务异常
     */
    public BusinessException toException() {
        return new BusinessException(this.code, this.message);
    }

    /**
     * 创建带自定义消息的业务异常
     */
    public BusinessException toException(String customMessage) {
        return new BusinessException(this.code, customMessage);
    }
}