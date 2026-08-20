package com.vrd.auth.controller;

import com.vrd.auth.dto.LoginRequest;
import com.vrd.auth.dto.LoginResponse;
import com.vrd.auth.dto.RegisterRequest;
import com.vrd.auth.dto.TokenIntrospectResponse;
import com.vrd.auth.dto.UserInfoResponse;
import com.vrd.auth.entity.User;
import com.vrd.auth.service.RoleService;
import com.vrd.auth.service.UserService;
import com.vrd.auth.util.JwtUtil;
import com.vrd.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    @Autowired
    private RoleService roleService;

    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        User user = this.userService.findByUsername(request.getUsername());
        if (user == null || !this.passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return Result.error(401, "用户名或密码错误");
        }
        if (user.getStatus() != 1) {
            return Result.error(403, "账号已被禁用");
        }
        String token = this.jwtUtil.generateToken(user.getUsername(), user.getId());
        user.setLastLoginTime(LocalDateTime.now());
        userService.updateById(user);
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUsername(user.getUsername());
        response.setUserId(user.getId());
        response.setExpiresIn(this.jwtUtil.getExpiration());
        return Result.success(response);
    }

    @PostMapping("/register")
    public Result<String> register(@RequestBody RegisterRequest request) {
        if (this.userService.findByUsername(request.getUsername()) != null) {
            return Result.error("用户名已存在");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(this.passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRealName(request.getRealName());
        user.setStatus(1);
        user.setDeleted(0);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        this.userService.save(user);
        return Result.success("注册成功");
    }

    @GetMapping("/validate")
    public Result<Boolean> validateToken(@RequestHeader(value = "Authorization") String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            return Result.success(false);
        }
        String jwt = token.substring(7);
        return Result.success(this.jwtUtil.validateToken(jwt));
    }

    @PostMapping("/introspect")
    public Result<TokenIntrospectResponse> introspectToken(@RequestParam(value = "token") String token) {
        TokenIntrospectResponse response = new TokenIntrospectResponse();
        try {
            if (token == null || token.isEmpty()) {
                response.setActive(false);
                return Result.success(response);
            }
            String jwt = token.startsWith("Bearer ") ? token.substring(7) : token;
            if (!this.jwtUtil.validateToken(jwt)) {
                response.setActive(false);
                return Result.success(response);
            }
            Long userId = this.jwtUtil.getUserIdFromToken(jwt);
            String username = this.jwtUtil.getUsernameFromToken(jwt);
            List<String> roles = this.roleService.getRoleNamesByUserId(userId);
            response.setActive(true);
            response.setUserId(userId);
            response.setUsername(username);
            response.setRoles(roles);
            response.setExpiresAt(this.jwtUtil.getExpirationDateFromToken(jwt).getTime());
            return Result.success(response);
        } catch (Exception e) {
            response.setActive(false);
            return Result.success(response);
        }
    }

    @GetMapping("/userinfo")
    public Result<UserInfoResponse> userInfo(@RequestHeader(value = "Authorization", required = false) String authorization, @RequestHeader(value = "X-User-Id", required = false) Long xUserId) {
        try {
            Long userId;
            if (xUserId != null) {
                // 经网关转发: 网关已完成 JWT 校验并通过 X-User-Id 传递用户标识
                userId = xUserId;
            } else if (authorization != null && authorization.startsWith("Bearer ")) {
                // 直连场景: 自行解析 JWT
                String jwt = authorization.substring(7);
                if (!this.jwtUtil.validateToken(jwt)) {
                    return Result.error(401, "token 已过期");
                }
                userId = this.jwtUtil.getUserIdFromToken(jwt);
            } else {
                return Result.error(401, "未登录或 token 无效");
            }
            User user = this.userService.getById(userId);
            if (user == null || user.getDeleted() == 1) {
                return Result.error(404, "用户不存在");
            }
            UserInfoResponse response = new UserInfoResponse();
            response.setUserId(user.getId());
            response.setUsername(user.getUsername());
            response.setRealName(user.getRealName());
            response.setEmail(user.getEmail());
            response.setPhone(user.getPhone());
            response.setRoles(this.roleService.getRoleNamesByUserId(userId));
            response.setPermissions(this.userService.getPermissionCodesByUserId(userId));
            return Result.success(response);
        } catch (Exception e) {
            return Result.error(401, "token 解析失败");
        }
    }
}
