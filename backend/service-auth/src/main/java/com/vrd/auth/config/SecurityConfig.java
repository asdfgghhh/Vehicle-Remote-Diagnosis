/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.context.annotation.Bean
 *  org.springframework.context.annotation.Configuration
 *  org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
 */
package com.vrd.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class SecurityConfig {
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

