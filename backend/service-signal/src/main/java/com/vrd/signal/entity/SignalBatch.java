package com.vrd.signal.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("signal_batch")
public class SignalBatch {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String vin;

    private Long vehicleId;

    private Integer signalCount;

    private String rawData;

    private String parsedData;

    private Integer status;

    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
