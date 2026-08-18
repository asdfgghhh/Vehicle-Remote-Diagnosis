package com.vrd.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserRoleMapper userRoleMapper;
    @Autowired
    private RoleService roleService;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public User findByUsername(String username) {
        return this.lambdaQuery().eq(User::getUsername, username).one();
    }

    @Override
    public Page<UserVO> pageUsers(Integer current, Integer size, String keyword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .eq(User::getDeleted, 0)
                .orderByDesc(User::getCreateTime);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                    .or().like(User::getRealName, keyword)
                    .or().like(User::getPhone, keyword));
        }
        Page<User> userPage = this.page(new Page<>(current, size), wrapper);
        Page<UserVO> voPage = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        voPage.setRecords(userPage.getRecords().stream().map(this::toUserVO).collect(Collectors.toList()));
        return voPage;
    }

    @Override
    public UserVO getUserDetail(Long id) {
        User user = this.getById(id);
        if (user == null || user.getDeleted() == 1) {
            throw new IllegalArgumentException("用户不存在");
        }
        return this.toUserVO(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO createUser(UserManageRequest request) {
        if (this.findByUsername(request.getUsername()) != null) {
            throw new IllegalArgumentException("用户名已存在");
        }
        if (!StringUtils.hasText(request.getPassword())) {
            throw new IllegalArgumentException("密码不能为空");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(this.passwordEncoder.encode(request.getPassword()));
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
    @Transactional(rollbackFor = Exception.class)
    public UserVO updateUser(Long id, UserManageRequest request) {
        User user = this.getById(id);
        if (user == null || user.getDeleted() == 1) {
            throw new IllegalArgumentException("用户不存在");
        }
        User existing = this.findByUsername(request.getUsername());
        if (existing != null && !existing.getId().equals(id)) {
            throw new IllegalArgumentException("用户名已存在");
        }
        user.setUsername(request.getUsername());
        if (StringUtils.hasText(request.getPassword())) {
            user.setPassword(this.passwordEncoder.encode(request.getPassword()));
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
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long id) {
        User user = this.getById(id);
        if (user != null) {
            user.setDeleted(1);
            user.setUpdateTime(LocalDateTime.now());
            this.updateById(user);
            this.userRoleMapper.delete(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, id));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRoles(Long userId, List<Long> roleIds) {
        this.userRoleMapper.delete(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId));
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
        List<UserRole> userRoles = this.userRoleMapper.selectList(
                new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, user.getId()));
        if (CollectionUtils.isEmpty(userRoles)) {
            vo.setRoleIds(Collections.emptyList());
            vo.setRoleNames(Collections.emptyList());
            return vo;
        }
        List<Long> roleIds = userRoles.stream().map(UserRole::getRoleId).collect(Collectors.toList());
        Map<Long, Role> roleMap = this.roleService.listByIds(roleIds).stream()
                .filter(role -> role.getDeleted() == 0)
                .collect(Collectors.toMap(Role::getId, role -> role));
        List<String> roleNames = new ArrayList<>();
        for (Long roleId : roleIds) {
            Role role = roleMap.get(roleId);
            if (role != null) {
                roleNames.add(role.getRoleName());
            }
        }
        vo.setRoleIds(roleIds);
        vo.setRoleNames(roleNames);
        return vo;
    }
}
