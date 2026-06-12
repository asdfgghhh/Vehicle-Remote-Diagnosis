package com.vrd.common.util;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

public class UserContext {

    private static final String X_USER_ID = "X-User-Id";
    private static final String X_USERNAME = "X-Username";
    private static final String X_ROLES = "X-Roles";
    private static final String GATEWAY_VALIDATED = "GATEWAY_VALIDATED";

    private UserContext() {
    }

    public static Long getUserId() {
        String userId = getHeader(X_USER_ID);
        return userId != null ? Long.parseLong(userId) : null;
    }

    public static String getUsername() {
        return getHeader(X_USERNAME);
    }

    public static List<String> getRoles() {
        String rolesBase64 = getHeader(X_ROLES);
        if (rolesBase64 == null || rolesBase64.isEmpty()) {
            return List.of();
        }
        try {
            String rolesStr = new String(Base64.getDecoder().decode(rolesBase64), StandardCharsets.UTF_8);
            return rolesStr.isEmpty() ? List.of() : Arrays.asList(rolesStr.split(","));
        } catch (Exception e) {
            return List.of();
        }
    }

    public static boolean isGatewayValidated() {
        String authorization = getHeader("Authorization");
        return authorization != null && authorization.contains(GATEWAY_VALIDATED);
    }

    public static boolean hasRole(String role) {
        List<String> roles = getRoles();
        return roles != null && roles.contains(role);
    }

    public static boolean hasAnyRole(String... roles) {
        List<String> userRoles = getRoles();
        if (userRoles == null || userRoles.isEmpty()) {
            return false;
        }
        for (String role : roles) {
            if (userRoles.contains(role)) {
                return true;
            }
        }
        return false;
    }

    private static String getHeader(String name) {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                return request.getHeader(name);
            }
        } catch (Exception e) {
        }
        return null;
    }
}
