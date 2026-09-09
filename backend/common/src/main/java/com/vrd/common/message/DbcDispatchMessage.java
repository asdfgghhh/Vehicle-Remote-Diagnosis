package com.vrd.common.message;

import java.io.Serializable;

/**
 * DBC 文件下发指令消息（service-dbc -> Kafka -> service-access -> MQTT）
 *
 * <p>遵循"MQTT 指令 + HTTPS 下载"分离模式：MQTT 只传小指令，DBC 文件由车端从对象存储下载。
 */
public class DbcDispatchMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 链路追踪 ID，与 dispatch_log.trace_id 对应 */
    private String traceId;
    /** 车辆 VIN */
    private String vin;
    /** DBC 文件 ID */
    private Long dbcFileId;
    /** DBC 文件版本号 */
    private String version;
    /** DBC 文件 HTTPS 下载地址（预签名 URL） */
    private String downloadUrl;
    /** 文件大小（字节） */
    private Long fileSize;
    /** SHA256 校验和（hex） */
    private String sha256;
    /** 车端目标存储路径，如 /etc/vrd/dbc/model_x.dbc */
    private String targetPath;
    /** 是否立即生效 */
    private Boolean immediateApply;
    /** 指令生成时间戳（毫秒） */
    private Long timestamp;

    public DbcDispatchMessage() {
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getVin() {
        return vin;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

    public Long getDbcFileId() {
        return dbcFileId;
    }

    public void setDbcFileId(Long dbcFileId) {
        this.dbcFileId = dbcFileId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getSha256() {
        return sha256;
    }

    public void setSha256(String sha256) {
        this.sha256 = sha256;
    }

    public String getTargetPath() {
        return targetPath;
    }

    public void setTargetPath(String targetPath) {
        this.targetPath = targetPath;
    }

    public Boolean getImmediateApply() {
        return immediateApply;
    }

    public void setImmediateApply(Boolean immediateApply) {
        this.immediateApply = immediateApply;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "DbcDispatchMessage{" +
                "traceId='" + traceId + '\'' +
                ", vin='" + vin + '\'' +
                ", dbcFileId=" + dbcFileId +
                ", version='" + version + '\'' +
                ", downloadUrl='" + downloadUrl + '\'' +
                ", fileSize=" + fileSize +
                ", sha256='" + sha256 + '\'' +
                ", targetPath='" + targetPath + '\'' +
                ", immediateApply=" + immediateApply +
                ", timestamp=" + timestamp +
                '}';
    }
}
