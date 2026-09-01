package com.vrd.vehicle.online;

/**
 * 车辆在线状态枚举
 * <p>
 * 使用3态：区分「确认离线」与「无数据/未知」场景。
 */
public enum VehicleOnlineStatus {

    /** 离线（下电） */
    OFFLINE(0, "离线"),

    /** 在线（上电运行中） */
    ONLINE(1, "在线"),

    /** 未知（系统刚录入车辆，无任何上报记录） */
    UNKNOWN(2, "未知");

    private final int code;
    private final String label;

    VehicleOnlineStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static VehicleOnlineStatus fromCode(Integer code) {
        if (code == null) {
            return UNKNOWN;
        }
        for (VehicleOnlineStatus s : values()) {
            if (s.code == code) {
                return s;
            }
        }
        return UNKNOWN;
    }
}
