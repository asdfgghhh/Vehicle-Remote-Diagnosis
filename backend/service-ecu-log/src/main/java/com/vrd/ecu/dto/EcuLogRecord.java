package com.vrd.ecu.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EcuLogRecord {

    private Long id;
    private String vin;
    /** 控制器缩写，如 EMS、BCM */
    private String ecuType;
    /** 日志内容开始时间 */
    private LocalDateTime logStartTime;
    /** 日志内容结束时间 */
    private LocalDateTime logEndTime;
    /** 上传开始时间 */
    private LocalDateTime uploadStartTime;
    /** 上传结束时间 */
    private LocalDateTime uploadEndTime;
    /** 文件存储地址（URL） */
    private String storageAddress;
    /** 对象存储 key，用于下载 */
    private String storageKey;
    /** 存储类型：ALIYUN_OSS / HUAWEI_OBS / MINIO / LOCAL */
    private String storageType;
    private String fileName;
    private Long fileSize;
    private String fileMd5;
}
