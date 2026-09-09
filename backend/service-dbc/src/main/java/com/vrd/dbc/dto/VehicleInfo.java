package com.vrd.dbc.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * service-vehicle 返回的车辆精简信息 DTO。
 * <p>仅包含 DBC 下发链路所需的字段（id、vin、modelId），
 * 使用 @JsonIgnoreProperties 忽略 Vehicle 实体中的其它字段，
 * 避免在 service-dbc 模块中直接依赖 service-vehicle 的 Vehicle 实体。
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class VehicleInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String vin;
    private Long modelId;

    public VehicleInfo() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVin() {
        return vin;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

    public Long getModelId() {
        return modelId;
    }

    public void setModelId(Long modelId) {
        this.modelId = modelId;
    }
}
