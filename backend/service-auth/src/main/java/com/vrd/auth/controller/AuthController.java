/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vrd.common.result.Result
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
 *  org.springframework.web.bind.annotation.GetMapping
 *  org.springframework.web.bind.annotation.PostMapping
 *  org.springframework.web.bind.annotation.RequestBody
 *  org.springframework.web.bind.annotation.RequestHeader
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RequestParam
 *  org.springframework.web.bind.annotation.RestController
 */
package com.vrd.auth.controller;

import com.vrd.auth.dto.LoginRequest;
import com.vrd.auth.dto.LoginResponse;
import com.vrd.auth.dto.RegisterRequest;
import com.vrd.auth.dto.TokenIntrospectResponse;
import com.vrd.auth.entity.User;
import com.vrd.auth.service.RoleService;
import com.vrd.auth.service.UserService;
import com.vrd.auth.util.JwtUtil;
import com.vrd.common.result.Result;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value={"/auth"})
public class AuthController {
    @Autowired
    private UserService userService;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    @Autowired
    private RoleService roleService;

    @PostMapping(value={"/login"})
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        User user = this.userService.findByUsername(request.getUsername());
        if (user == null || !this.passwordEncoder.matches((CharSequence)request.getPassword(), user.getPassword())) {
            return Result.error((Integer)401, (String)"\u7528\u6237\u540d\u6216\u5bc6\u7801\u9519\u8bef");
        }
        if (user.getStatus() != 1) {
            return Result.error((Integer)403, (String)"\u8d26\u53f7\u5df2\u88ab\u7981\u7528");
        }
        String token = this.jwtUtil.generateToken(user.getUsername(), user.getId());
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUsername(user.getUsername());
        response.setUserId(user.getId());
        response.setExpiresIn(this.jwtUtil.getExpiration());
        return Result.success((Object)response);
    }

    @PostMapping(value={"/register"})
    public Result<String> register(@RequestBody RegisterRequest request) {
        if (this.userService.findByUsername(request.getUsername()) != null) {
            return Result.error((String)"\u7528\u6237\u540d\u5df2\u5b58\u5728");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(this.passwordEncoder.encode((CharSequence)request.getPassword()));
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRealName(request.getRealName());
        user.setStatus(1);
        user.setDeleted(0);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        this.userService.save(user);
        return Result.success((Object)"\u6ce8\u518c\u6210\u529f");
    }

    @GetMapping(value={"/validate"})
    public Result<Boolean> validateToken(@RequestHeader(value="Authorization") String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            return Result.success((Object)false);
        }
        String jwt = token.substring(7);
        Boolean isValid = this.jwtUtil.validateToken(jwt);
        return Result.success((Object)isValid);
    }

    @PostMapping(value={"/introspect"})
    public Result<TokenIntrospectResponse> introspectToken(@RequestParam(value="token") String token) {
        TokenIntrospectResponse response = new TokenIntrospectResponse();
        try {
            String jwt;
            if (token == null || token.isEmpty()) {
                response.setActive(false);
                return Result.success((Object)response);
            }
            String string = jwt = token.startsWith("Bearer ") ? token.substring(7) : token;
            if (!this.jwtUtil.validateToken(jwt).booleanValue()) {
                response.setActive(false);
                return Result.success((Object)response);
            }
            Long userId = this.jwtUtil.getUserIdFromToken(jwt);
            String username = this.jwtUtil.getUsernameFromToken(jwt);
            List<String> roles = this.roleService.getRoleNamesByUserId(userId);
            response.setActive(true);
            response.setUserId(userId);
            response.setUsername(username);
            response.setRoles(roles);
            response.setExpiresAt(this.jwtUtil.getExpirationDateFromToken(jwt).getTime());
            return Result.success((Object)response);
        }
        catch (Exception e) {
            response.setActive(false);
            return Result.success((Object)response);
        }
    }
}

