/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.baomidou.mybatisplus.extension.plugins.pagination.Page
 *  com.vrd.common.result.Result
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.web.bind.annotation.DeleteMapping
 *  org.springframework.web.bind.annotation.GetMapping
 *  org.springframework.web.bind.annotation.PathVariable
 *  org.springframework.web.bind.annotation.PostMapping
 *  org.springframework.web.bind.annotation.PutMapping
 *  org.springframework.web.bind.annotation.RequestBody
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RequestParam
 *  org.springframework.web.bind.annotation.RestController
 */
package com.vrd.auth.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vrd.auth.dto.RoleRequest;
import com.vrd.auth.entity.Role;
import com.vrd.auth.service.RoleService;
import com.vrd.common.result.Result;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value={"/auth/role"})
public class RoleManageController {
    @Autowired
    private RoleService roleService;

    @GetMapping(value={"/page"})
    public Result<Page<Role>> page(@RequestParam(value="current", defaultValue="1") Integer current, @RequestParam(value="size", defaultValue="10") Integer size, @RequestParam(value="keyword", required=false) String keyword) {
        return Result.success(this.roleService.page(current, size, keyword));
    }

    @GetMapping(value={"/list"})
    public Result<List<Role>> list() {
        return Result.success(this.roleService.listEnabled());
    }

    @GetMapping(value={"/{id}"})
    public Result<Role> getById(@PathVariable Long id) {
        Role role = (Role)this.roleService.getById(id);
        if (role == null || role.getDeleted() == 1) {
            return Result.error((String)"\u89d2\u8272\u4e0d\u5b58\u5728");
        }
        return Result.success((Object)role);
    }

    @PostMapping
    public Result<Role> create(@RequestBody RoleRequest request) {
        try {
            return Result.success((Object)this.roleService.create(request));
        }
        catch (IllegalArgumentException e) {
            return Result.error((String)e.getMessage());
        }
    }

    @PutMapping(value={"/{id}"})
    public Result<Role> update(@PathVariable Long id, @RequestBody RoleRequest request) {
        try {
            return Result.success((Object)this.roleService.update(id, request));
        }
        catch (IllegalArgumentException e) {
            return Result.error((String)e.getMessage());
        }
    }

    @DeleteMapping(value={"/{id}"})
    public Result<Void> delete(@PathVariable Long id) {
        this.roleService.delete(id);
        return Result.success();
    }
}

