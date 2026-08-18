/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.boot.context.properties.ConfigurationProperties
 *  org.springframework.stereotype.Component
 */
package com.vrd.gateway.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix="gateway.auth")
public class AuthProperties {
    private boolean enabled = true;
    private String introspectUrl = "http://service-auth/auth/introspect";
    private List<String> whiteList = List.of("/api/auth/login", "/api/auth/register", "/actuator/**");
    private int timeout = 5000;

    public boolean isEnabled() {
        return this.enabled;
    }

    public String getIntrospectUrl() {
        return this.introspectUrl;
    }

    public List<String> getWhiteList() {
        return this.whiteList;
    }

    public int getTimeout() {
        return this.timeout;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setIntrospectUrl(String introspectUrl) {
        this.introspectUrl = introspectUrl;
    }

    public void setWhiteList(List<String> whiteList) {
        this.whiteList = whiteList;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof AuthProperties)) {
            return false;
        }
        AuthProperties other = (AuthProperties)o;
        if (!other.canEqual(this)) {
            return false;
        }
        if (this.isEnabled() != other.isEnabled()) {
            return false;
        }
        if (this.getTimeout() != other.getTimeout()) {
            return false;
        }
        String this$introspectUrl = this.getIntrospectUrl();
        String other$introspectUrl = other.getIntrospectUrl();
        if (this$introspectUrl == null ? other$introspectUrl != null : !this$introspectUrl.equals(other$introspectUrl)) {
            return false;
        }
        List<String> this$whiteList = this.getWhiteList();
        List<String> other$whiteList = other.getWhiteList();
        return !(this$whiteList == null ? other$whiteList != null : !((Object)this$whiteList).equals(other$whiteList));
    }

    protected boolean canEqual(Object other) {
        return other instanceof AuthProperties;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        result = result * 59 + (this.isEnabled() ? 79 : 97);
        result = result * 59 + this.getTimeout();
        String $introspectUrl = this.getIntrospectUrl();
        result = result * 59 + ($introspectUrl == null ? 43 : $introspectUrl.hashCode());
        List<String> $whiteList = this.getWhiteList();
        result = result * 59 + ($whiteList == null ? 43 : ((Object)$whiteList).hashCode());
        return result;
    }

    public String toString() {
        return "AuthProperties(enabled=" + this.isEnabled() + ", introspectUrl=" + this.getIntrospectUrl() + ", whiteList=" + String.valueOf(this.getWhiteList()) + ", timeout=" + this.getTimeout() + ")";
    }
}

