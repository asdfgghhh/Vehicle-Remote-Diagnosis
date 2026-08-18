/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.baomidou.mybatisplus.core.conditions.Wrapper
 *  com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper
 *  com.baomidou.mybatisplus.core.metadata.IPage
 *  com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper
 *  com.baomidou.mybatisplus.extension.plugins.pagination.Page
 *  com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.stereotype.Service
 *  org.springframework.util.StringUtils
 */
package com.vrd.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vrd.auth.dto.RoleRequest;
import com.vrd.auth.entity.Role;
import com.vrd.auth.entity.UserRole;
import com.vrd.auth.mapper.RoleMapper;
import com.vrd.auth.mapper.UserRoleMapper;
import com.vrd.auth.service.RoleService;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class RoleServiceImpl
extends ServiceImpl<RoleMapper, Role>
implements RoleService {
    @Autowired
    private UserRoleMapper userRoleMapper;

    @Override
    public Page<Role> page(Integer current, Integer size, String keyword) {
        LambdaQueryWrapper wrapper = (LambdaQueryWrapper)((LambdaQueryWrapper)new LambdaQueryWrapper().eq(Role::getDeleted, (Object)0)).orderByDesc(Role::getCreateTime);
        if (StringUtils.hasText((String)keyword)) {
            wrapper.and(w -> ((LambdaQueryWrapper)((LambdaQueryWrapper)w.like(Role::getRoleCode, (Object)keyword)).or()).like(Role::getRoleName, (Object)keyword));
        }
        return (Page)this.page((IPage)new Page((long)current.intValue(), (long)size.intValue()), (Wrapper)wrapper);
    }

    @Override
    public List<Role> listEnabled() {
        return ((LambdaQueryChainWrapper)((LambdaQueryChainWrapper)((LambdaQueryChainWrapper)this.lambdaQuery().eq(Role::getDeleted, (Object)0)).eq(Role::getStatus, (Object)1)).orderByAsc(Role::getRoleName)).list();
    }

    @Override
    public Role create(RoleRequest request) {
        if (((LambdaQueryChainWrapper)((LambdaQueryChainWrapper)this.lambdaQuery().eq(Role::getRoleCode, (Object)request.getRoleCode())).eq(Role::getDeleted, (Object)0)).exists()) {
            throw new IllegalArgumentException("\u89d2\u8272\u7f16\u7801\u5df2\u5b58\u5728");
        }
        Role role = this.buildRole(request);
        role.setCreateTime(LocalDateTime.now());
        role.setUpdateTime(LocalDateTime.now());
        this.save(role);
        return role;
    }

    @Override
    public Role update(Long id, RoleRequest request) {
        Role role = (Role)this.getById(id);
        if (role == null || role.getDeleted() == 1) {
            throw new IllegalArgumentException("\u89d2\u8272\u4e0d\u5b58\u5728");
        }
        boolean codeExists = ((LambdaQueryChainWrapper)((LambdaQueryChainWrapper)((LambdaQueryChainWrapper)this.lambdaQuery().eq(Role::getRoleCode, (Object)request.getRoleCode())).eq(Role::getDeleted, (Object)0)).ne(Role::getId, (Object)id)).exists();
        if (codeExists) {
            throw new IllegalArgumentException("\u89d2\u8272\u7f16\u7801\u5df2\u5b58\u5728");
        }
        role.setRoleCode(request.getRoleCode());
        role.setRoleName(request.getRoleName());
        role.setDescription(request.getDescription());
        role.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        role.setUpdateTime(LocalDateTime.now());
        this.updateById(role);
        return role;
    }

    @Override
    public void delete(Long id) {
        Role role = (Role)this.getById(id);
        if (role != null) {
            role.setDeleted(1);
            role.setUpdateTime(LocalDateTime.now());
            this.updateById(role);
        }
    }

    private Role buildRole(RoleRequest request) {
        Role role = new Role();
        role.setRoleCode(request.getRoleCode());
        role.setRoleName(request.getRoleName());
        role.setDescription(request.getDescription());
        role.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        role.setDeleted(0);
        return role;
    }

    @Override
    public List<String> getRoleNamesByUserId(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        List userRoles = this.userRoleMapper.selectList((Wrapper)((LambdaQueryWrapper)new LambdaQueryWrapper().eq(UserRole::getUserId, (Object)userId)).eq(UserRole::getIsDeleted, (Object)0));
        if (userRoles.isEmpty()) {
            return Collections.emptyList();
        }
        List roleIds = userRoles.stream().map(UserRole::getRoleId).collect(Collectors.toList());
        List roles = ((LambdaQueryChainWrapper)((LambdaQueryChainWrapper)this.lambdaQuery().in(Role::getId, roleIds)).eq(Role::getDeleted, (Object)0)).list();
        return roles.stream().map(Role::getRoleName).collect(Collectors.toList());
    }
}

