package com.vrd.vehicle.controller;

import com.vrd.common.result.Result;
import com.vrd.vehicle.dto.FaultAnalysisDistributionVO;
import com.vrd.vehicle.dto.RiskVehicleVO;
import com.vrd.vehicle.dto.VehicleAlertLongTrendVO;
import com.vrd.vehicle.service.VehicleAnalysisService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 车辆故障分析接口（服务端口 9082，网关路由前缀 /api/vehicle/fault-analysis、/api/vehicle/risk）
 * <p>
 * 提供故障趋势、故障分布、风险车辆列表等统计分析能力，支撑分析报表页面。
 */
@RestController
@RequestMapping(value={"/vehicle"})
public class VehicleAnalysisController {
    @Autowired
    private VehicleAnalysisService vehicleAnalysisService;

    /**
     * 查询故障趋势统计
     * <p>GET /vehicle/fault-analysis/trend
     *
     * @param granularity 粒度，可选 hour / day / month，默认 day
     * @return VehicleAlertLongTrendVO 故障数时序数据
     */
    @GetMapping(value={"/fault-analysis/trend"})
    public Result<VehicleAlertLongTrendVO> faultTrend(@RequestParam(value="granularity", defaultValue="day") String granularity) {
        return Result.success(this.vehicleAnalysisService.getFaultTrend(granularity));
    }

    /**
     * 查询故障分布统计
     * <p>GET /vehicle/fault-analysis/distribution
     *
     * @return FaultAnalysisDistributionVO 按故障码/车型/域维度的分布数据
     */
    @GetMapping(value={"/fault-analysis/distribution"})
    public Result<FaultAnalysisDistributionVO> faultDistribution() {
        return Result.success(this.vehicleAnalysisService.getFaultDistribution());
    }

    /**
     * 查询高风险车辆列表
     * <p>GET /vehicle/risk/list
     *
     * @return List&lt;RiskVehicleVO&gt; 按风险评分排序的高风险车辆
     */
    @GetMapping(value={"/risk/list"})
    public Result<List<RiskVehicleVO>> riskList() {
        return Result.success(this.vehicleAnalysisService.listRiskVehicles());
    }
}
