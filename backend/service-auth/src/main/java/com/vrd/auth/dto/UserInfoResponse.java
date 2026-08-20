package com.vrd.auth.dto;

import java.util.List;

public class UserInfoResponse {
    private Long userId;
    private String username;
    private String realName;
    private String email;
    private String phone;
    private List<String> roles;
    private List<String> permissions;

    public Long getUserId() { return this.userId; }
    public String getUsername() { return this.username; }
    public String getRealName() { return this.realName; }
    public String getEmail() { return this.email; }
    public String getPhone() { return this.phone; }
    public List<String> getRoles() { return this.roles; }
    public List<String> getPermissions() { return this.permissions; }

    public void setUserId(Long userId) { this.userId = userId; }
    public void setUsername(String username) { this.username = username; }
    public void setRealName(String realName) { this.realName = realName; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setRoles(List<String> roles) { this.roles = roles; }
    public void setPermissions(List<String> permissions) { this.permissions = permissions; }
}
