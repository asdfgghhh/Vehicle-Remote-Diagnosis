package com.vrd.ecu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ecu_log_file")
public class EcuLogFile {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String fileName;

    private String filePath;

    private Long fileSize;

    private String md5;

    private Long vehicleId;

    private String vin;

    private String ecuType;

    private Integer uploadStatus;

    private Long uploadedSize;

    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
