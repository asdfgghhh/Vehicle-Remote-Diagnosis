package com.vrd.auth.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.vrd.auth.dto.RoleRequest;
import com.vrd.auth.entity.Role;

import java.util.List;

public interface RoleService extends IService<Role> {

    Page<Role> page(Integer current, Integer size, String keyword);

    List<Role> listEnabled();

    Role create(RoleRequest request);

    Role update(Long id, RoleRequest request);

    void delete(Long id);
}
