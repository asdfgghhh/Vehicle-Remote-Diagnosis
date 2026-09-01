package com.vrd.vehicle.controller;

import com.vrd.common.result.Result;
import com.vrd.vehicle.dto.VehicleDomainHealthVO;
import com.vrd.vehicle.dto.VehicleHealthDetailVO;
import com.vrd.vehicle.dto.VehicleHealthTrendVO;
import com.vrd.vehicle.service.VehicleHealthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 车辆健康度接口（服务端口 9082，网关路由前缀 /api/vehicle/health）
 * <p>
 * 基于车辆信号与故障数据计算整车/域的健康评分、评分趋势与域详情。
 */
@RestController
@RequestMapping(value={"/vehicle/health"})
public class VehicleHealthController {
    @Autowired
    private VehicleHealthService vehicleHealthService;

    /**
     * 查询车辆健康度详情
     * <p>GET /vehicle/health/{vin}
     *
     * @param vin 车架号
     * @return VehicleHealthDetailVO 整车健康评分与各域评分概览
     */
    @GetMapping(value={"/{vin}"})
    public Result<VehicleHealthDetailVO> healthDetail(@PathVariable(value="vin") String vin) {
        return Result.success(this.vehicleHealthService.getHealthDetail(vin));
    }

    /**
     * 查询车辆健康度评分趋势
     * <p>GET /vehicle/health/{vin}/trend
     *
     * @param vin  车架号
     * @param days 可选，统计天数，缺省由服务层决定
     * @return VehicleHealthTrendVO 健康评分时序数据
     */
    @GetMapping(value={"/{vin}/trend"})
    public Result<VehicleHealthTrendVO> healthTrend(@PathVariable(value="vin") String vin, @RequestParam(value="days", required=false) Integer days) {
        return Result.success(this.vehicleHealthService.getHealthTrend(vin, days));
    }

    /**
     * 查询车辆指定域的健康详情
     * <p>GET /vehicle/health/domain/{vin}/{domainCode}
     *
     * @param vin        车架号
     * @param domainCode 域编码（如动力域、底盘域、车身域等）
     * @return VehicleDomainHealthVO 该域的健康评分与信号明细
     */
    @GetMapping(value={"/domain/{vin}/{domainCode}"})
    public Result<VehicleDomainHealthVO> domainDetail(@PathVariable(value="vin") String vin, @PathVariable(value="domainCode") String domainCode) {
        return Result.success(this.vehicleHealthService.getDomainDetail(vin, domainCode));
    }
}
