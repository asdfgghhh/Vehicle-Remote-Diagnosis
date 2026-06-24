package com.vrd.gateway.config;

import com.vrd.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Slf4j
@Configuration
@Order(-1)
public class GatewayErrorConfiguration implements ErrorWebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String message = ErrorCode.INTERNAL_ERROR.getMessage();
        
        if (ex instanceof ResponseStatusException) {
            ResponseStatusException rsp = (ResponseStatusException) ex;
            status = HttpStatus.valueOf(rsp.getStatusCode().value());
            ErrorCode errorCode = ErrorCode.fromStatusCode(status.value());
            message = errorCode.getMessage();
        } else if (ex instanceof org.springframework.web.server.MethodNotAllowedException) {
            status = HttpStatus.METHOD_NOT_ALLOWED;
            message = ErrorCode.METHOD_NOT_ALLOWED.getMessage();
        } else if (ex instanceof org.springframework.web.server.UnsupportedMediaTypeStatusException) {
            status = HttpStatus.UNSUPPORTED_MEDIA_TYPE;
            message = ErrorCode.UNSUPPORTED_MEDIA_TYPE.getMessage();
        } else if (ex instanceof org.springframework.web.server.ServerWebInputException) {
            status = HttpStatus.BAD_REQUEST;
            message = ErrorCode.BAD_REQUEST.getMessage();
        }
        
        log.warn("网关异常 - 路径: {}, 状态: {}, 错误: {}", 
                exchange.getRequest().getPath(), status, ex.getMessage());
        
        String json = "{\"code\":" + status.value() + ",\"message\":\"" + escapeJson(message) + "\",\"data\":null}";
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(json.getBytes(StandardCharsets.UTF_8));
        
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}