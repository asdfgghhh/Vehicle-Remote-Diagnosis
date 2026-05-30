package com.vrd.dbc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("dispatch_log")
public class DispatchLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long dbcFileId;

    private Long vehicleId;

    private String vin;

    private String dispatchType;

    private Integer status;

    private String result;

    private LocalDateTime dispatchTime;

    private LocalDateTime createTime;
}
