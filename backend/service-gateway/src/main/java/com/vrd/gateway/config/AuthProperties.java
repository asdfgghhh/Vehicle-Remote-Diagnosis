package com.vrd.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "gateway.auth")
public class AuthProperties {

    private boolean enabled = true;
    
    private String introspectUrl = "http://service-auth/auth/introspect";
    
    private List<String> whiteList = List.of(
            "/api/auth/login",
            "/api/auth/register",
            "/actuator/**"
    );
    
    private int timeout = 5000;
}
