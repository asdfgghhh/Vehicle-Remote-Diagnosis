package com.vrd.vehicle.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("fault_config")
public class FaultConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long modelId;

    /** 故障码 */
    private String faultCode;

    /** DTC */
    private String dtc;

    /** 告警名称 */
    private String alarmName;

    /** ECU 部件 */
    private String ecuType;

    /** 部件英文简称 */
    private String componentCode;

    /** 1-严重 2-警告 3-提示 */
    private Integer alarmLevel;

    private String description;

    /** 1-启用 0-禁用 */
    private Integer status;

    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String modelName;
}
