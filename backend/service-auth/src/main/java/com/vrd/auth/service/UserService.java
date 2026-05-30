package com.vrd.auth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vrd.auth.entity.User;

public interface UserService extends IService<User> {
    User findByUsername(String username);
}
