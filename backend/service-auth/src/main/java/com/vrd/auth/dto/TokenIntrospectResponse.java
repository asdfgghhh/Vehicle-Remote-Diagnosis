/*
 * Decompiled with CFR 0.152.
 */
package com.vrd.auth.dto;

import java.util.List;

public class TokenIntrospectResponse {
    private boolean active;
    private Long userId;
    private String username;
    private List<String> roles;
    private Long expiresAt;

    public boolean isActive() {
        return this.active;
    }

    public Long getUserId() {
        return this.userId;
    }

    public String getUsername() {
        return this.username;
    }

    public List<String> getRoles() {
        return this.roles;
    }

    public Long getExpiresAt() {
        return this.expiresAt;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public void setExpiresAt(Long expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof TokenIntrospectResponse)) {
            return false;
        }
        TokenIntrospectResponse other = (TokenIntrospectResponse)o;
        if (!other.canEqual(this)) {
            return false;
        }
        if (this.isActive() != other.isActive()) {
            return false;
        }
        Long this$userId = this.getUserId();
        Long other$userId = other.getUserId();
        if (this$userId == null ? other$userId != null : !((Object)this$userId).equals(other$userId)) {
            return false;
        }
        Long this$expiresAt = this.getExpiresAt();
        Long other$expiresAt = other.getExpiresAt();
        if (this$expiresAt == null ? other$expiresAt != null : !((Object)this$expiresAt).equals(other$expiresAt)) {
            return false;
        }
        String this$username = this.getUsername();
        String other$username = other.getUsername();
        if (this$username == null ? other$username != null : !this$username.equals(other$username)) {
            return false;
        }
        List<String> this$roles = this.getRoles();
        List<String> other$roles = other.getRoles();
        return !(this$roles == null ? other$roles != null : !((Object)this$roles).equals(other$roles));
    }

    protected boolean canEqual(Object other) {
        return other instanceof TokenIntrospectResponse;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        result = result * 59 + (this.isActive() ? 79 : 97);
        Long $userId = this.getUserId();
        result = result * 59 + ($userId == null ? 43 : ((Object)$userId).hashCode());
        Long $expiresAt = this.getExpiresAt();
        result = result * 59 + ($expiresAt == null ? 43 : ((Object)$expiresAt).hashCode());
        String $username = this.getUsername();
        result = result * 59 + ($username == null ? 43 : $username.hashCode());
        List<String> $roles = this.getRoles();
        result = result * 59 + ($roles == null ? 43 : ((Object)$roles).hashCode());
        return result;
    }

    public String toString() {
        return "TokenIntrospectResponse(active=" + this.isActive() + ", userId=" + this.getUserId() + ", username=" + this.getUsername() + ", roles=" + String.valueOf(this.getRoles()) + ", expiresAt=" + this.getExpiresAt() + ")";
    }
}

