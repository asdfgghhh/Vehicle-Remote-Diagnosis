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
import com.vrd.auth.dto.UserManageRequest;
import com.vrd.auth.dto.UserVO;
import com.vrd.auth.service.UserService;
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
@RequestMapping(value={"/auth/user"})
public class UserManageController {
    @Autowired
    private UserService userService;

    @GetMapping(value={"/page"})
    public Result<Page<UserVO>> page(@RequestParam(value="current", defaultValue="1") Integer current, @RequestParam(value="size", defaultValue="10") Integer size, @RequestParam(value="keyword", required=false) String keyword) {
        return Result.success(this.userService.pageUsers(current, size, keyword));
    }

    @GetMapping(value={"/{id}"})
    public Result<UserVO> getById(@PathVariable Long id) {
        try {
            return Result.success((Object)this.userService.getUserDetail(id));
        }
        catch (IllegalArgumentException e) {
            return Result.error((String)e.getMessage());
        }
    }

    @PostMapping
    public Result<UserVO> create(@RequestBody UserManageRequest request) {
        try {
            return Result.success((Object)this.userService.createUser(request));
        }
        catch (IllegalArgumentException e) {
            return Result.error((String)e.getMessage());
        }
    }

    @PutMapping(value={"/{id}"})
    public Result<UserVO> update(@PathVariable Long id, @RequestBody UserManageRequest request) {
        try {
            return Result.success((Object)this.userService.updateUser(id, request));
        }
        catch (IllegalArgumentException e) {
            return Result.error((String)e.getMessage());
        }
    }

    @DeleteMapping(value={"/{id}"})
    public Result<Void> delete(@PathVariable Long id) {
        this.userService.deleteUser(id);
        return Result.success();
    }

    @PutMapping(value={"/{id}/roles"})
    public Result<Void> assignRoles(@PathVariable Long id, @RequestBody List<Long> roleIds) {
        this.userService.assignRoles(id, roleIds);
        return Result.success();
    }
}

