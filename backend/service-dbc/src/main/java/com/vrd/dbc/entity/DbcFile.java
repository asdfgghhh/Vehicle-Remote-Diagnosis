package com.vrd.dbc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("dbc_file")
public class DbcFile {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联车型 ID（vrd_vehicle.vehicle_model） */
    private Long modelId;

    /** 车型名称（冗余存储，便于列表展示） */
    private String modelName;

    private String fileName;

    /** 对象存储 key */
    private String storageKey;

    /** 对象存储访问地址 */
    private String storageAddress;

    /** 存储类型：MINIO / ALIYUN_OSS / TENCENT_COS 等 */
    private String storageType;

    /** 兼容历史数据，新上传与 storageKey 一致 */
    private String filePath;

    private Long fileSize;

    private String version;

    private String description;

    private String parseResult;

    private Integer messageCount;

    private Integer signalCount;

    private Integer status;

    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
