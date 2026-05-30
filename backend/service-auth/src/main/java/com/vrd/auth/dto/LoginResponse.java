package com.vrd.auth.dto;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private String username;
    private Long userId;
    private Long expiresIn;
}
