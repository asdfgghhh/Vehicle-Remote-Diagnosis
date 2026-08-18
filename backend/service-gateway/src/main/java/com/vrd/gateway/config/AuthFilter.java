/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.core.JsonProcessingException
 *  com.fasterxml.jackson.databind.JsonNode
 *  com.fasterxml.jackson.databind.ObjectMapper
 *  org.reactivestreams.Publisher
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.cloud.gateway.filter.GatewayFilterChain
 *  org.springframework.cloud.gateway.filter.GlobalFilter
 *  org.springframework.core.Ordered
 *  org.springframework.http.HttpStatus
 *  org.springframework.http.HttpStatusCode
 *  org.springframework.http.MediaType
 *  org.springframework.http.server.reactive.ServerHttpRequest
 *  org.springframework.http.server.reactive.ServerHttpResponse
 *  org.springframework.stereotype.Component
 *  org.springframework.web.reactive.function.client.WebClient
 *  org.springframework.web.reactive.function.client.WebClient$RequestBodySpec
 *  org.springframework.web.server.ServerWebExchange
 *  reactor.core.publisher.Mono
 */
package com.vrd.gateway.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vrd.gateway.config.AuthProperties;
import com.vrd.gateway.config.TokenIntrospectResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthFilter
implements GlobalFilter,
Ordered {
    private static final Logger log = LoggerFactory.getLogger(AuthFilter.class);
    private final AuthProperties authProperties;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private static final String X_USER_ID = "X-User-Id";
    private static final String X_USERNAME = "X-Username";
    private static final String X_ROLES = "X-Roles";

    public AuthFilter(AuthProperties authProperties, ObjectMapper objectMapper) {
        this.authProperties = authProperties;
        this.objectMapper = objectMapper;
        this.webClient = WebClient.builder().codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(0x1000000)).build();
    }

    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        if (!this.authProperties.isEnabled()) {
            return chain.filter(exchange);
        }
        if (this.isWhiteListed(path)) {
            log.debug("Path {} is whitelisted, skipping auth check", (Object)path);
            return chain.filter(exchange);
        }
        String token = request.getHeaders().getFirst("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            log.warn("Missing or invalid Authorization header for path: {}", (Object)path);
            return this.unauthorizedResponse(exchange);
        }
        String jwtToken = token.substring(7);
        return this.introspectToken(jwtToken).flatMap(introspectResponse -> {
            if (introspectResponse.isActive()) {
                ServerHttpRequest modifiedRequest = this.modifyRequestWithUserInfo(request, (TokenIntrospectResponse)introspectResponse);
                log.debug("Token validated successfully for user: {}", (Object)introspectResponse.getUsername());
                return chain.filter(exchange.mutate().request(modifiedRequest).build());
            }
            log.warn("Token validation failed for path: {}", (Object)path);
            return this.unauthorizedResponse(exchange);
        }).onErrorResume(e -> {
            log.error("Token introspection failed", e);
            return this.unauthorizedResponse(exchange);
        });
    }

    private Mono<TokenIntrospectResponse> introspectToken(String token) {
        String encodedToken = URLEncoder.encode(token, StandardCharsets.UTF_8);
        return ((WebClient.RequestBodySpec)((WebClient.RequestBodySpec)this.webClient.post().uri(this.authProperties.getIntrospectUrl() + "?token=" + encodedToken, new Object[0])).header("Content-Type", new String[]{"application/x-www-form-urlencoded"})).retrieve().bodyToMono(String.class).map(responseBody -> {
            try {
                return this.objectMapper.readTree(responseBody);
            }
            catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to parse introspect response", e);
            }
        }).map(jsonNode -> {
            TokenIntrospectResponse response = new TokenIntrospectResponse();
            response.setActive(jsonNode.has("data") ? jsonNode.get("data").get("active").asBoolean(false) : false);
            if (response.isActive() && jsonNode.has("data")) {
                JsonNode dataNode = jsonNode.get("data");
                response.setUserId(dataNode.has("userId") ? Long.valueOf(dataNode.get("userId").asLong()) : null);
                response.setUsername(dataNode.has("username") ? dataNode.get("username").asText() : null);
                if (dataNode.has("roles")) {
                    JsonNode rolesArray = dataNode.get("roles");
                    response.setRoles(rolesArray.findValuesAsText(""));
                }
                response.setExpiresAt(dataNode.has("expiresAt") ? Long.valueOf(dataNode.get("expiresAt").asLong()) : null);
            }
            return response;
        });
    }

    private ServerHttpRequest modifyRequestWithUserInfo(ServerHttpRequest request, TokenIntrospectResponse introspectResponse) {
        return request.mutate().header(X_USER_ID, new String[]{String.valueOf(introspectResponse.getUserId())}).header(X_USERNAME, new String[]{introspectResponse.getUsername()}).header(X_ROLES, new String[]{this.serializeRoles(introspectResponse.getRoles())}).header("Authorization", new String[]{"Bearer GATEWAY_VALIDATED"}).build();
    }

    private String serializeRoles(List<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return "";
        }
        return Base64.getEncoder().encodeToString(String.join((CharSequence)",", roles).getBytes(StandardCharsets.UTF_8));
    }

    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode((HttpStatusCode)HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"code\": 401, \"message\": \"Unauthorized\", \"data\": null}";
        return response.writeWith((Publisher)Mono.just((Object)response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8))));
    }

    private boolean isWhiteListed(String path) {
        return this.authProperties.getWhiteList().stream().anyMatch(pattern -> {
            if (pattern.endsWith("**")) {
                String prefix = pattern.substring(0, pattern.length() - 2);
                return path.startsWith(prefix);
            }
            return path.equals(pattern);
        });
    }

    public int getOrder() {
        return -100;
    }
}

