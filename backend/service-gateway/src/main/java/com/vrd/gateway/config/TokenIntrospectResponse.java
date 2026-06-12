package com.vrd.gateway.config;

import lombok.Data;

import java.util.List;

@Data
public class TokenIntrospectResponse {
    private boolean active;
    private Long userId;
    private String username;
    private List<String> roles;
    private Long expiresAt;
}
