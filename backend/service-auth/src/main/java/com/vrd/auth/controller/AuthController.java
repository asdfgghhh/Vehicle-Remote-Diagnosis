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

/**
 * 认证授权接口（服务端口 9081，网关路由前缀 /api/auth）
 * <p>
 * 提供用户登录注册、JWT 令牌校验/内省、当前用户信息查询等基础认证能力。
 * 生产环境经 service-gateway 的 AuthFilter 完成 JWT 校验后，通过 X-User-Id 信任头传递用户身份；
 * 直连本服务的场景下才自行解析 Authorization 头中的 JWT。
 */
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

    /**
     * 用户登录
     * <p>POST /auth/login
     *
     * @param request 登录请求体（username 用户名、password 明文密码）
     * @return LoginResponse：token（JWT）、username、userId、expiresIn（有效期，秒）
     * <ul>
     *   <li>用户名或密码错误返回 code=401</li>
     *   <li>账号被禁用（status != 1）返回 code=403</li>
     * </ul>
     */
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

    /**
     * 用户注册
     * <p>POST /auth/register
     *
     * @param request 注册请求体（username、password、email、phone、realName）
     * @return 注册结果消息；用户名已存在时返回错误
     */
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

    /**
     * 校验 JWT 令牌有效性
     * <p>GET /auth/validate
     *
     * @param token Authorization 请求头，格式 "Bearer {jwt}"
     * @return Boolean：true=令牌有效；false=缺失或无效
     */
    @GetMapping("/validate")
    public Result<Boolean> validateToken(@RequestHeader(value = "Authorization") String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            return Result.success(false);
        }
        String jwt = token.substring(7);
        return Result.success(this.jwtUtil.validateToken(jwt));
    }

    /**
     * 令牌内省（Token Introspection）
     * <p>POST /auth/introspect?token={jwt}
     * <p>解析令牌并返回用户身份与角色信息，供网关或下游服务鉴权使用。
     *
     * @param token JWT 令牌（可带或不带 "Bearer " 前缀）
     * @return TokenIntrospectResponse：active、userId、username、roles、expiresAt（毫秒时间戳）
     */
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

    /**
     * 查询当前登录用户信息
     * <p>GET /auth/userinfo
     * <p>优先读取网关信任头 X-User-Id；无该头时回退解析 Authorization 中的 JWT（直连场景）。
     *
     * @param authorization Authorization 请求头（直连时必填）
     * @param xUserId       网关注入的用户 ID 信任头
     * @return UserInfoResponse：userId、username、realName、email、phone、roles（角色名列表）、permissions（权限码列表）
     */
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
