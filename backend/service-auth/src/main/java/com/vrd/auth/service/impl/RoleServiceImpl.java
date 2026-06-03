package com.vrd.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vrd.auth.dto.RoleRequest;
import com.vrd.auth.entity.Role;
import com.vrd.auth.mapper.RoleMapper;
import com.vrd.auth.service.RoleService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    @Override
    public Page<Role> page(Integer current, Integer size, String keyword) {
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<Role>()
                .eq(Role::getDeleted, 0)
                .orderByDesc(Role::getCreateTime);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Role::getRoleCode, keyword)
                    .or().like(Role::getRoleName, keyword));
        }
        return page(new Page<>(current, size), wrapper);
    }

    @Override
    public java.util.List<Role> listEnabled() {
        return lambdaQuery()
                .eq(Role::getDeleted, 0)
                .eq(Role::getStatus, 1)
                .orderByAsc(Role::getRoleName)
                .list();
    }

    @Override
    public Role create(RoleRequest request) {
        if (lambdaQuery().eq(Role::getRoleCode, request.getRoleCode()).eq(Role::getDeleted, 0).exists()) {
            throw new IllegalArgumentException("角色编码已存在");
        }
        Role role = buildRole(request);
        role.setCreateTime(LocalDateTime.now());
        role.setUpdateTime(LocalDateTime.now());
        save(role);
        return role;
    }

    @Override
    public Role update(Long id, RoleRequest request) {
        Role role = getById(id);
        if (role == null || role.getDeleted() == 1) {
            throw new IllegalArgumentException("角色不存在");
        }
        boolean codeExists = lambdaQuery()
                .eq(Role::getRoleCode, request.getRoleCode())
                .eq(Role::getDeleted, 0)
                .ne(Role::getId, id)
                .exists();
        if (codeExists) {
            throw new IllegalArgumentException("角色编码已存在");
        }
        role.setRoleCode(request.getRoleCode());
        role.setRoleName(request.getRoleName());
        role.setDescription(request.getDescription());
        role.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        role.setUpdateTime(LocalDateTime.now());
        updateById(role);
        return role;
    }

    @Override
    public void delete(Long id) {
        Role role = getById(id);
        if (role != null) {
            role.setDeleted(1);
            role.setUpdateTime(LocalDateTime.now());
            updateById(role);
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
}
