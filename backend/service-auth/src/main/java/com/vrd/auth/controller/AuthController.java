package com.vrd.auth.controller;

import com.vrd.auth.dto.LoginRequest;
import com.vrd.auth.dto.LoginResponse;
import com.vrd.auth.dto.RegisterRequest;
import com.vrd.auth.entity.User;
import com.vrd.auth.service.UserService;
import com.vrd.auth.util.JwtUtil;
import com.vrd.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        User user = userService.findByUsername(request.getUsername());

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return Result.error(401, "用户名或密码错误");
        }

        if (user.getStatus() != 1) {
            return Result.error(403, "账号已被禁用");
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getId());

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUsername(user.getUsername());
        response.setUserId(user.getId());
        response.setExpiresIn(jwtUtil.getExpiration());

        return Result.success(response);
    }

    @PostMapping("/register")
    public Result<String> register(@RequestBody RegisterRequest request) {
        if (userService.findByUsername(request.getUsername()) != null) {
            return Result.error("用户名已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRealName(request.getRealName());
        user.setStatus(1);
        user.setDeleted(0);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        userService.save(user);

        return Result.success("注册成功");
    }

    @GetMapping("/validate")
    public Result<Boolean> validateToken(@RequestHeader("Authorization") String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            return Result.success(false);
        }

        String jwt = token.substring(7);
        Boolean isValid = jwtUtil.validateToken(jwt);
        return Result.success(isValid);
    }
}
