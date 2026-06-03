package com.vrd.auth.dto;

import lombok.Data;

@Data
public class RoleRequest {
    private String roleCode;
    private String roleName;
    private String description;
    private Integer status;
}
