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
import com.vrd.auth.dto.RoleRequest;
import com.vrd.auth.entity.Role;
import java.util.List;

public interface RoleService
extends IService<Role> {
    public Page<Role> page(Integer var1, Integer var2, String var3);

    public List<Role> listEnabled();

    public Role create(RoleRequest var1);

    public Role update(Long var1, RoleRequest var2);

    public void delete(Long var1);

    public List<String> getRoleNamesByUserId(Long var1);
}

