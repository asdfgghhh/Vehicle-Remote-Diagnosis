package com.vrd.gateway.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

@Slf4j
@Component
public class AuthFilter implements GlobalFilter, Ordered {

    private final AuthProperties authProperties;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public AuthFilter(AuthProperties authProperties, ObjectMapper objectMapper) {
        this.authProperties = authProperties;
        this.objectMapper = objectMapper;
        this.webClient = WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                .build();
    }

    private static final String X_USER_ID = "X-User-Id";
    private static final String X_USERNAME = "X-Username";
    private static final String X_ROLES = "X-Roles";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        if (!authProperties.isEnabled()) {
            return chain.filter(exchange);
        }

        if (isWhiteListed(path)) {
            log.debug("Path {} is whitelisted, skipping auth check", path);
            return chain.filter(exchange);
        }

        String token = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (token == null || !token.startsWith("Bearer ")) {
            log.warn("Missing or invalid Authorization header for path: {}", path);
            return unauthorizedResponse(exchange);
        }

        String jwtToken = token.substring(7);
        
        return introspectToken(jwtToken)
                .flatMap(introspectResponse -> {
                    if (introspectResponse.isActive()) {
                        ServerHttpRequest modifiedRequest = modifyRequestWithUserInfo(request, introspectResponse);
                        log.debug("Token validated successfully for user: {}", introspectResponse.getUsername());
                        return chain.filter(exchange.mutate().request(modifiedRequest).build());
                    } else {
                        log.warn("Token validation failed for path: {}", path);
                        return unauthorizedResponse(exchange);
                    }
                })
                .onErrorResume(e -> {
                    log.error("Token introspection failed", e);
                    return unauthorizedResponse(exchange);
                });
    }

    private Mono<TokenIntrospectResponse> introspectToken(String token) {
        String encodedToken = URLEncoder.encode(token, StandardCharsets.UTF_8);
        
        return webClient.post()
                .uri(authProperties.getIntrospectUrl() + "?token=" + encodedToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .retrieve()
                .bodyToMono(String.class)
                .map(responseBody -> {
                    try {
                        return objectMapper.readTree(responseBody);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException("Failed to parse introspect response", e);
                    }
                })
                .map(jsonNode -> {
                    TokenIntrospectResponse response = new TokenIntrospectResponse();
                    response.setActive(jsonNode.has("data") ? jsonNode.get("data").get("active").asBoolean(false) : false);
                    
                    if (response.isActive() && jsonNode.has("data")) {
                        var dataNode = jsonNode.get("data");
                        response.setUserId(dataNode.has("userId") ? dataNode.get("userId").asLong() : null);
                        response.setUsername(dataNode.has("username") ? dataNode.get("username").asText() : null);
                        
                        if (dataNode.has("roles")) {
                            var rolesArray = dataNode.get("roles");
                            response.setRoles(rolesArray.findValuesAsText(""));
                        }
                        
                        response.setExpiresAt(dataNode.has("expiresAt") ? dataNode.get("expiresAt").asLong() : null);
                    }
                    return response;
                });
    }

    private ServerHttpRequest modifyRequestWithUserInfo(ServerHttpRequest request, TokenIntrospectResponse introspectResponse) {
        return request.mutate()
                .header(X_USER_ID, String.valueOf(introspectResponse.getUserId()))
                .header(X_USERNAME, introspectResponse.getUsername())
                .header(X_ROLES, serializeRoles(introspectResponse.getRoles()))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + "GATEWAY_VALIDATED")
                .build();
    }

    private String serializeRoles(List<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return "";
        }
        return Base64.getEncoder().encodeToString(String.join(",", roles).getBytes(StandardCharsets.UTF_8));
    }

    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        
        String body = "{\"code\": 401, \"message\": \"Unauthorized\", \"data\": null}";
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8))));
    }

    private boolean isWhiteListed(String path) {
        return authProperties.getWhiteList().stream().anyMatch(pattern -> {
            if (pattern.endsWith("**")) {
                String prefix = pattern.substring(0, pattern.length() - 2);
                return path.startsWith(prefix);
            }
            return path.equals(pattern);
        });
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
