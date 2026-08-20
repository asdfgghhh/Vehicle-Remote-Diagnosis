package com.vrd.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName(value="sys_permission")
public class SysPermission {
    @TableId(type=IdType.AUTO)
    private Long id;
    private Long parentId;
    private String permCode;
    private String permName;
    private Integer permType;
    private String routePath;
    private String icon;
    private Integer sortNo;
    private Integer status;
    private Integer deleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Long getId() { return this.id; }
    public Long getParentId() { return this.parentId; }
    public String getPermCode() { return this.permCode; }
    public String getPermName() { return this.permName; }
    public Integer getPermType() { return this.permType; }
    public String getRoutePath() { return this.routePath; }
    public String getIcon() { return this.icon; }
    public Integer getSortNo() { return this.sortNo; }
    public Integer getStatus() { return this.status; }
    public Integer getDeleted() { return this.deleted; }
    public LocalDateTime getCreateTime() { return this.createTime; }
    public LocalDateTime getUpdateTime() { return this.updateTime; }

    public void setId(Long id) { this.id = id; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public void setPermCode(String permCode) { this.permCode = permCode; }
    public void setPermName(String permName) { this.permName = permName; }
    public void setPermType(Integer permType) { this.permType = permType; }
    public void setRoutePath(String routePath) { this.routePath = routePath; }
    public void setIcon(String icon) { this.icon = icon; }
    public void setSortNo(Integer sortNo) { this.sortNo = sortNo; }
    public void setStatus(Integer status) { this.status = status; }
    public void setDeleted(Integer deleted) { this.deleted = deleted; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
