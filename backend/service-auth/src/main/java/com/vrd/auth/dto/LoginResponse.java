/*
 * Decompiled with CFR 0.152.
 */
package com.vrd.auth.dto;

public class LoginResponse {
    private String token;
    private String username;
    private Long userId;
    private Long expiresIn;

    public String getToken() {
        return this.token;
    }

    public String getUsername() {
        return this.username;
    }

    public Long getUserId() {
        return this.userId;
    }

    public Long getExpiresIn() {
        return this.expiresIn;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof LoginResponse)) {
            return false;
        }
        LoginResponse other = (LoginResponse)o;
        if (!other.canEqual(this)) {
            return false;
        }
        Long this$userId = this.getUserId();
        Long other$userId = other.getUserId();
        if (this$userId == null ? other$userId != null : !((Object)this$userId).equals(other$userId)) {
            return false;
        }
        Long this$expiresIn = this.getExpiresIn();
        Long other$expiresIn = other.getExpiresIn();
        if (this$expiresIn == null ? other$expiresIn != null : !((Object)this$expiresIn).equals(other$expiresIn)) {
            return false;
        }
        String this$token = this.getToken();
        String other$token = other.getToken();
        if (this$token == null ? other$token != null : !this$token.equals(other$token)) {
            return false;
        }
        String this$username = this.getUsername();
        String other$username = other.getUsername();
        return !(this$username == null ? other$username != null : !this$username.equals(other$username));
    }

    protected boolean canEqual(Object other) {
        return other instanceof LoginResponse;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Long $userId = this.getUserId();
        result = result * 59 + ($userId == null ? 43 : ((Object)$userId).hashCode());
        Long $expiresIn = this.getExpiresIn();
        result = result * 59 + ($expiresIn == null ? 43 : ((Object)$expiresIn).hashCode());
        String $token = this.getToken();
        result = result * 59 + ($token == null ? 43 : $token.hashCode());
        String $username = this.getUsername();
        result = result * 59 + ($username == null ? 43 : $username.hashCode());
        return result;
    }

    public String toString() {
        return "LoginResponse(token=" + this.getToken() + ", username=" + this.getUsername() + ", userId=" + this.getUserId() + ", expiresIn=" + this.getExpiresIn() + ")";
    }
}

