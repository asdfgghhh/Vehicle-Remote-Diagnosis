package com.vrd.auth.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vrd.auth.dto.RoleRequest;
import com.vrd.auth.entity.Role;
import com.vrd.auth.service.RoleService;
import com.vrd.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理接口（服务端口 9081，网关路由前缀 /api/auth/role）
 * <p>
 * 提供 RBAC 角色的增删改查能力，角色关联权限码用于前端菜单/按钮级鉴权。
 */
@RestController
@RequestMapping("/auth/role")
public class RoleManageController {

    @Autowired
    private RoleService roleService;

    /**
     * 分页查询角色列表
     * <p>GET /auth/role/page
     *
     * @param current 当前页码，默认 1
     * @param size    每页条数，默认 10
     * @param keyword 可选，按角色名/编码模糊过滤
     * @return 分页结果 Page&lt;Role&gt;
     */
    @GetMapping("/page")
    public Result<Page<Role>> page(@RequestParam(value = "current", defaultValue = "1") Integer current,
                                   @RequestParam(value = "size", defaultValue = "10") Integer size,
                                   @RequestParam(value = "keyword", required = false) String keyword) {
        return Result.success(this.roleService.page(current, size, keyword));
    }

    /**
     * 查询全部启用状态的角色（不分页）
     * <p>GET /auth/role/list
     * <p>常用于用户管理页面的角色下拉选择。
     *
     * @return 角色 List&lt;Role&gt;
     */
    @GetMapping("/list")
    public Result<List<Role>> list() {
        return Result.success(this.roleService.listEnabled());
    }

    /**
     * 查询角色详情
     * <p>GET /auth/role/{id}
     *
     * @param id 角色 ID
     * @return Role 角色详情；不存在或已删除时返回错误
     */
    @GetMapping("/{id}")
    public Result<Role> getById(@PathVariable Long id) {
        Role role = this.roleService.getById(id);
        if (role == null || role.getDeleted() == 1) {
            return Result.error("角色不存在");
        }
        return Result.success(role);
    }

    /**
     * 创建角色
     * <p>POST /auth/role
     *
     * @param request 角色信息（roleName、roleCode、status、权限码列表等）
     * @return 创建成功后的 Role；参数不合法时返回错误
     */
    @PostMapping
    public Result<Role> create(@RequestBody RoleRequest request) {
        try {
            return Result.success(this.roleService.create(request));
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新角色信息
     * <p>PUT /auth/role/{id}
     *
     * @param id      角色 ID
     * @param request 待更新的角色字段
     * @return 更新后的 Role
     */
    @PutMapping("/{id}")
    public Result<Role> update(@PathVariable Long id, @RequestBody RoleRequest request) {
        try {
            return Result.success(this.roleService.update(id, request));
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除角色（逻辑删除）
     * <p>DELETE /auth/role/{id}
     *
     * @param id 角色 ID
     * @return 空结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        this.roleService.delete(id);
        return Result.success();
    }
}
