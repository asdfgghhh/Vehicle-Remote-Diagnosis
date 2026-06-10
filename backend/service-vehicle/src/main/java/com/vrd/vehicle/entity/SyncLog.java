package com.vrd.vehicle.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sync_log")
public class SyncLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String syncType;

    private String source;

    private String target;

    private String vin;

    private String action;

    private Integer recordCount;

    private String status;

    private String message;

    private String payload;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private LocalDateTime createTime;
}
