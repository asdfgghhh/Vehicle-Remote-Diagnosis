package com.vrd.auth.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vrd.auth.dto.RoleRequest;
import com.vrd.auth.entity.Role;
import com.vrd.auth.service.RoleService;
import com.vrd.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth/role")
public class RoleManageController {

    @Autowired
    private RoleService roleService;

    @GetMapping("/page")
    public Result<Page<Role>> page(
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "keyword", required = false) String keyword) {
        return Result.success(roleService.page(current, size, keyword));
    }

    @GetMapping("/list")
    public Result<List<Role>> list() {
        return Result.success(roleService.listEnabled());
    }

    @GetMapping("/{id}")
    public Result<Role> getById(@PathVariable Long id) {
        Role role = roleService.getById(id);
        if (role == null || role.getDeleted() == 1) {
            return Result.error("角色不存在");
        }
        return Result.success(role);
    }

    @PostMapping
    public Result<Role> create(@RequestBody RoleRequest request) {
        try {
            return Result.success(roleService.create(request));
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result<Role> update(@PathVariable Long id, @RequestBody RoleRequest request) {
        try {
            return Result.success(roleService.update(id, request));
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return Result.success();
    }
}
