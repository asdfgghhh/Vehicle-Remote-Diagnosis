package com.vrd.common.exception.config;

import com.vrd.common.exception.GlobalExceptionHandler;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 异常处理自动配置类
 * 确保全局异常处理器被正确加载
 * 仅在 Servlet 环境下生效（WebFlux 网关如 service-gateway 不加载）
 */
@Configuration
@ConditionalOnClass(HttpServletRequest.class)
public class ExceptionAutoConfiguration {

    /**
     * 注册全局异常处理器
     * 此配置确保在 common 模块被依赖时异常处理器能被正确注册
     */
    @Bean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }
}
