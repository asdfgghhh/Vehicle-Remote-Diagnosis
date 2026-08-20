package com.vrd.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName(value="sys_role_permission")
public class SysRolePermission {
    @TableId(type=IdType.AUTO)
    private Long id;
    private Long roleId;
    private Long permissionId;
    private LocalDateTime createTime;

    public Long getId() { return this.id; }
    public Long getRoleId() { return this.roleId; }
    public Long getPermissionId() { return this.permissionId; }
    public LocalDateTime getCreateTime() { return this.createTime; }

    public void setId(Long id) { this.id = id; }
    public void setRoleId(Long roleId) { this.roleId = roleId; }
    public void setPermissionId(Long permissionId) { this.permissionId = permissionId; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
