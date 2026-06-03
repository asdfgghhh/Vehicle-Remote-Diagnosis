package com.vrd.auth.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.vrd.auth.dto.UserManageRequest;
import com.vrd.auth.dto.UserVO;
import com.vrd.auth.entity.User;

import java.util.List;

public interface UserService extends IService<User> {

    User findByUsername(String username);

    Page<UserVO> pageUsers(Integer current, Integer size, String keyword);

    UserVO getUserDetail(Long id);

    UserVO createUser(UserManageRequest request);

    UserVO updateUser(Long id, UserManageRequest request);

    void deleteUser(Long id);

    void assignRoles(Long userId, List<Long> roleIds);
}
