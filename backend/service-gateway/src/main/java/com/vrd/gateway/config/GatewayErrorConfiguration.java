/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vrd.common.exception.ErrorCode
 *  org.reactivestreams.Publisher
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler
 *  org.springframework.context.annotation.Configuration
 *  org.springframework.core.annotation.Order
 *  org.springframework.core.io.buffer.DataBuffer
 *  org.springframework.http.HttpStatus
 *  org.springframework.http.HttpStatusCode
 *  org.springframework.http.MediaType
 *  org.springframework.web.server.MethodNotAllowedException
 *  org.springframework.web.server.ResponseStatusException
 *  org.springframework.web.server.ServerWebExchange
 *  org.springframework.web.server.ServerWebInputException
 *  org.springframework.web.server.UnsupportedMediaTypeStatusException
 *  reactor.core.publisher.Mono
 */
package com.vrd.gateway.config;

import com.vrd.common.exception.ErrorCode;
import java.nio.charset.StandardCharsets;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.server.MethodNotAllowedException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;
import reactor.core.publisher.Mono;

@Configuration
@Order(value=-1)
public class GatewayErrorConfiguration
implements ErrorWebExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GatewayErrorConfiguration.class);

    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String message = ErrorCode.INTERNAL_ERROR.getMessage();
        if (ex instanceof ResponseStatusException) {
            ResponseStatusException rsp = (ResponseStatusException)ex;
            status = HttpStatus.valueOf((int)rsp.getStatusCode().value());
            ErrorCode errorCode = ErrorCode.fromStatusCode((int)status.value());
            message = errorCode.getMessage();
        } else if (ex instanceof MethodNotAllowedException) {
            status = HttpStatus.METHOD_NOT_ALLOWED;
            message = ErrorCode.METHOD_NOT_ALLOWED.getMessage();
        } else if (ex instanceof UnsupportedMediaTypeStatusException) {
            status = HttpStatus.UNSUPPORTED_MEDIA_TYPE;
            message = ErrorCode.UNSUPPORTED_MEDIA_TYPE.getMessage();
        } else if (ex instanceof ServerWebInputException) {
            status = HttpStatus.BAD_REQUEST;
            message = ErrorCode.BAD_REQUEST.getMessage();
        }
        log.warn("\u7f51\u5173\u5f02\u5e38 - \u8def\u5f84: {}, \u72b6\u6001: {}, \u9519\u8bef: {}", new Object[]{exchange.getRequest().getPath(), status, ex.getMessage()});
        String json = "{\"code\":" + status.value() + ",\"message\":\"" + this.escapeJson(message) + "\",\"data\":null}";
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(json.getBytes(StandardCharsets.UTF_8));
        exchange.getResponse().setStatusCode((HttpStatusCode)status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        return exchange.getResponse().writeWith((Publisher)Mono.just((Object)buffer));
    }

    private String escapeJson(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }
}

