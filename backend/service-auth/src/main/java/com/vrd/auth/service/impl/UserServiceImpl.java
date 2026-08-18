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
 *  org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
 *  org.springframework.stereotype.Service
 *  org.springframework.transaction.annotation.Transactional
 *  org.springframework.util.CollectionUtils
 *  org.springframework.util.StringUtils
 */
package com.vrd.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vrd.auth.dto.UserManageRequest;
import com.vrd.auth.dto.UserVO;
import com.vrd.auth.entity.Role;
import com.vrd.auth.entity.User;
import com.vrd.auth.entity.UserRole;
import com.vrd.auth.mapper.UserMapper;
import com.vrd.auth.mapper.UserRoleMapper;
import com.vrd.auth.service.RoleService;
import com.vrd.auth.service.UserService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Service
public class UserServiceImpl
extends ServiceImpl<UserMapper, User>
implements UserService {
    @Autowired
    private UserRoleMapper userRoleMapper;
    @Autowired
    private RoleService roleService;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public User findByUsername(String username) {
        return (User)((LambdaQueryChainWrapper)this.lambdaQuery().eq(User::getUsername, (Object)username)).one();
    }

    @Override
    public Page<UserVO> pageUsers(Integer current, Integer size, String keyword) {
        LambdaQueryWrapper wrapper = (LambdaQueryWrapper)((LambdaQueryWrapper)new LambdaQueryWrapper().eq(User::getDeleted, (Object)0)).orderByDesc(User::getCreateTime);
        if (StringUtils.hasText((String)keyword)) {
            wrapper.and(w -> ((LambdaQueryWrapper)((LambdaQueryWrapper)((LambdaQueryWrapper)((LambdaQueryWrapper)w.like(User::getUsername, (Object)keyword)).or()).like(User::getRealName, (Object)keyword)).or()).like(User::getPhone, (Object)keyword));
        }
        Page userPage = (Page)this.page((IPage)new Page((long)current.intValue(), (long)size.intValue()), (Wrapper)wrapper);
        Page voPage = new Page(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        voPage.setRecords(userPage.getRecords().stream().map(this::toUserVO).collect(Collectors.toList()));
        return voPage;
    }

    @Override
    public UserVO getUserDetail(Long id) {
        User user = (User)this.getById(id);
        if (user == null || user.getDeleted() == 1) {
            throw new IllegalArgumentException("\u7528\u6237\u4e0d\u5b58\u5728");
        }
        return this.toUserVO(user);
    }

    @Override
    @Transactional(rollbackFor={Exception.class})
    public UserVO createUser(UserManageRequest request) {
        if (this.findByUsername(request.getUsername()) != null) {
            throw new IllegalArgumentException("\u7528\u6237\u540d\u5df2\u5b58\u5728");
        }
        if (!StringUtils.hasText((String)request.getPassword())) {
            throw new IllegalArgumentException("\u5bc6\u7801\u4e0d\u80fd\u4e3a\u7a7a");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(this.passwordEncoder.encode((CharSequence)request.getPassword()));
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRealName(request.getRealName());
        user.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        user.setDeleted(0);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        this.save(user);
        this.assignRoles(user.getId(), request.getRoleIds());
        return this.getUserDetail(user.getId());
    }

    @Override
    @Transactional(rollbackFor={Exception.class})
    public UserVO updateUser(Long id, UserManageRequest request) {
        User user = (User)this.getById(id);
        if (user == null || user.getDeleted() == 1) {
            throw new IllegalArgumentException("\u7528\u6237\u4e0d\u5b58\u5728");
        }
        User existing = this.findByUsername(request.getUsername());
        if (existing != null && !existing.getId().equals(id)) {
            throw new IllegalArgumentException("\u7528\u6237\u540d\u5df2\u5b58\u5728");
        }
        user.setUsername(request.getUsername());
        if (StringUtils.hasText((String)request.getPassword())) {
            user.setPassword(this.passwordEncoder.encode((CharSequence)request.getPassword()));
        }
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRealName(request.getRealName());
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }
        user.setUpdateTime(LocalDateTime.now());
        this.updateById(user);
        this.assignRoles(id, request.getRoleIds());
        return this.getUserDetail(id);
    }

    @Override
    @Transactional(rollbackFor={Exception.class})
    public void deleteUser(Long id) {
        User user = (User)this.getById(id);
        if (user != null) {
            user.setDeleted(1);
            user.setUpdateTime(LocalDateTime.now());
            this.updateById(user);
            this.userRoleMapper.delete((Wrapper)new LambdaQueryWrapper().eq(UserRole::getUserId, (Object)id));
        }
    }

    @Override
    @Transactional(rollbackFor={Exception.class})
    public void assignRoles(Long userId, List<Long> roleIds) {
        this.userRoleMapper.delete((Wrapper)new LambdaQueryWrapper().eq(UserRole::getUserId, (Object)userId));
        if (CollectionUtils.isEmpty(roleIds)) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        for (Long roleId : roleIds) {
            UserRole userRole = new UserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            userRole.setCreateTime(now);
            this.userRoleMapper.insert(userRole);
        }
    }

    private UserVO toUserVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setRealName(user.getRealName());
        vo.setStatus(user.getStatus());
        vo.setCreateTime(user.getCreateTime());
        vo.setUpdateTime(user.getUpdateTime());
        List userRoles = this.userRoleMapper.selectList((Wrapper)new LambdaQueryWrapper().eq(UserRole::getUserId, (Object)user.getId()));
        if (CollectionUtils.isEmpty((Collection)userRoles)) {
            vo.setRoleIds(Collections.emptyList());
            vo.setRoleNames(Collections.emptyList());
            return vo;
        }
        List<Long> roleIds = userRoles.stream().map(UserRole::getRoleId).collect(Collectors.toList());
        Map<Long, Role> roleMap = this.roleService.listByIds(roleIds).stream().filter(role -> role.getDeleted() == 0).collect(Collectors.toMap(Role::getId, role -> role));
        ArrayList<String> roleNames = new ArrayList<String>();
        for (Long roleId : roleIds) {
            Role role2 = roleMap.get(roleId);
            if (role2 == null) continue;
            roleNames.add(role2.getRoleName());
        }
        vo.setRoleIds(roleIds);
        vo.setRoleNames(roleNames);
        return vo;
    }
}

