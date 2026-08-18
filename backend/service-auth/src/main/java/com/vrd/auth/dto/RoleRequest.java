/*
 * Decompiled with CFR 0.152.
 */
package com.vrd.auth.dto;

public class RoleRequest {
    private String roleCode;
    private String roleName;
    private String description;
    private Integer status;

    public String getRoleCode() {
        return this.roleCode;
    }

    public String getRoleName() {
        return this.roleName;
    }

    public String getDescription() {
        return this.description;
    }

    public Integer getStatus() {
        return this.status;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof RoleRequest)) {
            return false;
        }
        RoleRequest other = (RoleRequest)o;
        if (!other.canEqual(this)) {
            return false;
        }
        Integer this$status = this.getStatus();
        Integer other$status = other.getStatus();
        if (this$status == null ? other$status != null : !((Object)this$status).equals(other$status)) {
            return false;
        }
        String this$roleCode = this.getRoleCode();
        String other$roleCode = other.getRoleCode();
        if (this$roleCode == null ? other$roleCode != null : !this$roleCode.equals(other$roleCode)) {
            return false;
        }
        String this$roleName = this.getRoleName();
        String other$roleName = other.getRoleName();
        if (this$roleName == null ? other$roleName != null : !this$roleName.equals(other$roleName)) {
            return false;
        }
        String this$description = this.getDescription();
        String other$description = other.getDescription();
        return !(this$description == null ? other$description != null : !this$description.equals(other$description));
    }

    protected boolean canEqual(Object other) {
        return other instanceof RoleRequest;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Integer $status = this.getStatus();
        result = result * 59 + ($status == null ? 43 : ((Object)$status).hashCode());
        String $roleCode = this.getRoleCode();
        result = result * 59 + ($roleCode == null ? 43 : $roleCode.hashCode());
        String $roleName = this.getRoleName();
        result = result * 59 + ($roleName == null ? 43 : $roleName.hashCode());
        String $description = this.getDescription();
        result = result * 59 + ($description == null ? 43 : $description.hashCode());
        return result;
    }

    public String toString() {
        return "RoleRequest(roleCode=" + this.getRoleCode() + ", roleName=" + this.getRoleName() + ", description=" + this.getDescription() + ", status=" + this.getStatus() + ")";
    }
}

