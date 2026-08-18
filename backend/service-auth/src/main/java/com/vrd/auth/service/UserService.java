/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.baomidou.mybatisplus.extension.plugins.pagination.Page
 *  com.baomidou.mybatisplus.extension.service.IService
 */
package com.vrd.auth.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.vrd.auth.dto.UserManageRequest;
import com.vrd.auth.dto.UserVO;
import com.vrd.auth.entity.User;
import java.util.List;

public interface UserService
extends IService<User> {
    public User findByUsername(String var1);

    public Page<UserVO> pageUsers(Integer var1, Integer var2, String var3);

    public UserVO getUserDetail(Long var1);

    public UserVO createUser(UserManageRequest var1);

    public UserVO updateUser(Long var1, UserManageRequest var2);

    public void deleteUser(Long var1);

    public void assignRoles(Long var1, List<Long> var2);
}

