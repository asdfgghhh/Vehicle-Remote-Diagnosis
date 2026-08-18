/*
 * Decompiled with CFR 0.152.
 */
package com.vrd.auth.dto;

import java.time.LocalDateTime;
import java.util.List;

public class UserVO {
    private Long id;
    private String username;
    private String email;
    private String phone;
    private String realName;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<Long> roleIds;
    private List<String> roleNames;

    public Long getId() {
        return this.id;
    }

    public String getUsername() {
        return this.username;
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

    public LocalDateTime getCreateTime() {
        return this.createTime;
    }

    public LocalDateTime getUpdateTime() {
        return this.updateTime;
    }

    public List<Long> getRoleIds() {
        return this.roleIds;
    }

    public List<String> getRoleNames() {
        return this.roleNames;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public void setRoleIds(List<Long> roleIds) {
        this.roleIds = roleIds;
    }

    public void setRoleNames(List<String> roleNames) {
        this.roleNames = roleNames;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof UserVO)) {
            return false;
        }
        UserVO other = (UserVO)o;
        if (!other.canEqual(this)) {
            return false;
        }
        Long this$id = this.getId();
        Long other$id = other.getId();
        if (this$id == null ? other$id != null : !((Object)this$id).equals(other$id)) {
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
        LocalDateTime this$createTime = this.getCreateTime();
        LocalDateTime other$createTime = other.getCreateTime();
        if (this$createTime == null ? other$createTime != null : !((Object)this$createTime).equals(other$createTime)) {
            return false;
        }
        LocalDateTime this$updateTime = this.getUpdateTime();
        LocalDateTime other$updateTime = other.getUpdateTime();
        if (this$updateTime == null ? other$updateTime != null : !((Object)this$updateTime).equals(other$updateTime)) {
            return false;
        }
        List<Long> this$roleIds = this.getRoleIds();
        List<Long> other$roleIds = other.getRoleIds();
        if (this$roleIds == null ? other$roleIds != null : !((Object)this$roleIds).equals(other$roleIds)) {
            return false;
        }
        List<String> this$roleNames = this.getRoleNames();
        List<String> other$roleNames = other.getRoleNames();
        return !(this$roleNames == null ? other$roleNames != null : !((Object)this$roleNames).equals(other$roleNames));
    }

    protected boolean canEqual(Object other) {
        return other instanceof UserVO;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Long $id = this.getId();
        result = result * 59 + ($id == null ? 43 : ((Object)$id).hashCode());
        Integer $status = this.getStatus();
        result = result * 59 + ($status == null ? 43 : ((Object)$status).hashCode());
        String $username = this.getUsername();
        result = result * 59 + ($username == null ? 43 : $username.hashCode());
        String $email = this.getEmail();
        result = result * 59 + ($email == null ? 43 : $email.hashCode());
        String $phone = this.getPhone();
        result = result * 59 + ($phone == null ? 43 : $phone.hashCode());
        String $realName = this.getRealName();
        result = result * 59 + ($realName == null ? 43 : $realName.hashCode());
        LocalDateTime $createTime = this.getCreateTime();
        result = result * 59 + ($createTime == null ? 43 : ((Object)$createTime).hashCode());
        LocalDateTime $updateTime = this.getUpdateTime();
        result = result * 59 + ($updateTime == null ? 43 : ((Object)$updateTime).hashCode());
        List<Long> $roleIds = this.getRoleIds();
        result = result * 59 + ($roleIds == null ? 43 : ((Object)$roleIds).hashCode());
        List<String> $roleNames = this.getRoleNames();
        result = result * 59 + ($roleNames == null ? 43 : ((Object)$roleNames).hashCode());
        return result;
    }

    public String toString() {
        return "UserVO(id=" + this.getId() + ", username=" + this.getUsername() + ", email=" + this.getEmail() + ", phone=" + this.getPhone() + ", realName=" + this.getRealName() + ", status=" + this.getStatus() + ", createTime=" + String.valueOf(this.getCreateTime()) + ", updateTime=" + String.valueOf(this.getUpdateTime()) + ", roleIds=" + String.valueOf(this.getRoleIds()) + ", roleNames=" + String.valueOf(this.getRoleNames()) + ")";
    }
}

