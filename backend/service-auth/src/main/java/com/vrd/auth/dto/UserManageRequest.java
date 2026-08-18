/*
 * Decompiled with CFR 0.152.
 */
package com.vrd.auth.dto;

import java.util.List;

public class UserManageRequest {
    private String username;
    private String password;
    private String email;
    private String phone;
    private String realName;
    private Integer status;
    private List<Long> roleIds;

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public String getEmail() {
        return this.email;
    }

    public String getPhone() {
        return this.phone;
    }

    public String getRealName() {
        return this.realName;
    }

    public Integer getStatus() {
        return this.status;
    }

    public List<Long> getRoleIds() {
        return this.roleIds;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public void setRoleIds(List<Long> roleIds) {
        this.roleIds = roleIds;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof UserManageRequest)) {
            return false;
        }
        UserManageRequest other = (UserManageRequest)o;
        if (!other.canEqual(this)) {
            return false;
        }
        Integer this$status = this.getStatus();
        Integer other$status = other.getStatus();
        if (this$status == null ? other$status != null : !((Object)this$status).equals(other$status)) {
            return false;
        }
        String this$username = this.getUsername();
        String other$username = other.getUsername();
        if (this$username == null ? other$username != null : !this$username.equals(other$username)) {
            return false;
        }
        String this$password = this.getPassword();
        String other$password = other.getPassword();
        if (this$password == null ? other$password != null : !this$password.equals(other$password)) {
            return false;
        }
        String this$email = this.getEmail();
        String other$email = other.getEmail();
        if (this$email == null ? other$email != null : !this$email.equals(other$email)) {
            return false;
        }
        String this$phone = this.getPhone();
        String other$phone = other.getPhone();
        if (this$phone == null ? other$phone != null : !this$phone.equals(other$phone)) {
            return false;
        }
        String this$realName = this.getRealName();
        String other$realName = other.getRealName();
        if (this$realName == null ? other$realName != null : !this$realName.equals(other$realName)) {
            return false;
        }
        List<Long> this$roleIds = this.getRoleIds();
        List<Long> other$roleIds = other.getRoleIds();
        return !(this$roleIds == null ? other$roleIds != null : !((Object)this$roleIds).equals(other$roleIds));
    }

    protected boolean canEqual(Object other) {
        return other instanceof UserManageRequest;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Integer $status = this.getStatus();
        result = result * 59 + ($status == null ? 43 : ((Object)$status).hashCode());
        String $username = this.getUsername();
        result = result * 59 + ($username == null ? 43 : $username.hashCode());
        String $password = this.getPassword();
        result = result * 59 + ($password == null ? 43 : $password.hashCode());
        String $email = this.getEmail();
        result = result * 59 + ($email == null ? 43 : $email.hashCode());
        String $phone = this.getPhone();
        result = result * 59 + ($phone == null ? 43 : $phone.hashCode());
        String $realName = this.getRealName();
        result = result * 59 + ($realName == null ? 43 : $realName.hashCode());
        List<Long> $roleIds = this.getRoleIds();
        result = result * 59 + ($roleIds == null ? 43 : ((Object)$roleIds).hashCode());
        return result;
    }

    public String toString() {
        return "UserManageRequest(username=" + this.getUsername() + ", password=" + this.getPassword() + ", email=" + this.getEmail() + ", phone=" + this.getPhone() + ", realName=" + this.getRealName() + ", status=" + this.getStatus() + ", roleIds=" + String.valueOf(this.getRoleIds()) + ")";
    }
}

