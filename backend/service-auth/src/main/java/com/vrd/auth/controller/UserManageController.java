package com.vrd.auth.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vrd.auth.dto.UserManageRequest;
import com.vrd.auth.dto.UserVO;
import com.vrd.auth.service.UserService;
import com.vrd.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户管理接口（服务端口 9081，网关路由前缀 /api/auth/user）
 * <p>
 * 面向管理后台的用户 CRUD 与角色分配操作。
 */
@RestController
@RequestMapping("/auth/user")
public class UserManageController {

    @Autowired
    private UserService userService;

    /**
     * 分页查询用户列表
     * <p>GET /auth/user/page
     *
     * @param current 当前页码，默认 1
     * @param size    每页条数，默认 10
     * @param keyword 可选，按用户名/姓名模糊过滤
     * @return 分页结果 Page&lt;UserVO&gt;
     */
    @GetMapping("/page")
    public Result<Page<UserVO>> page(@RequestParam(value = "current", defaultValue = "1") Integer current,
                                     @RequestParam(value = "size", defaultValue = "10") Integer size,
                                     @RequestParam(value = "keyword", required = false) String keyword) {
        return Result.success(this.userService.pageUsers(current, size, keyword));
    }

    /**
     * 查询用户详情（含角色信息）
     * <p>GET /auth/user/{id}
     *
     * @param id 用户 ID
     * @return UserVO 用户详情；用户不存在时返回错误
     */
    @GetMapping("/{id}")
    public Result<UserVO> getById(@PathVariable Long id) {
        try {
            return Result.success(this.userService.getUserDetail(id));
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 创建用户
     * <p>POST /auth/user
     *
     * @param request 用户信息（username、password、email、phone、realName、status 等）
     * @return 创建成功后的 UserVO；参数不合法（如用户名重复）时返回错误
     */
    @PostMapping
    public Result<UserVO> create(@RequestBody UserManageRequest request) {
        try {
            return Result.success(this.userService.createUser(request));
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新用户信息
     * <p>PUT /auth/user/{id}
     *
     * @param id      用户 ID
     * @param request 待更新的用户字段
     * @return 更新后的 UserVO
     */
    @PutMapping("/{id}")
    public Result<UserVO> update(@PathVariable Long id, @RequestBody UserManageRequest request) {
        try {
            return Result.success(this.userService.updateUser(id, request));
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除用户（逻辑删除）
     * <p>DELETE /auth/user/{id}
     *
     * @param id 用户 ID
     * @return 空结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        this.userService.deleteUser(id);
        return Result.success();
    }

    /**
     * 为用户分配角色（全量覆盖式）
     * <p>PUT /auth/user/{id}/roles
     *
     * @param id      用户 ID
     * @param roleIds 角色 ID 列表
     * @return 空结果
     */
    @PutMapping("/{id}/roles")
    public Result<Void> assignRoles(@PathVariable Long id, @RequestBody List<Long> roleIds) {
        this.userService.assignRoles(id, roleIds);
        return Result.success();
    }
}
