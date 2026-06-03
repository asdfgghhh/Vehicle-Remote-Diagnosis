package com.vrd.auth.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserManageRequest {
    private String username;
    private String password;
    private String email;
    private String phone;
    private String realName;
    private Integer status;
    private List<Long> roleIds;
}
