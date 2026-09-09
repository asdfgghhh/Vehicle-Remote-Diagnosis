/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper
 *  com.baomidou.mybatisplus.extension.plugins.pagination.Page
 *  com.vrd.common.result.Result
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.web.bind.annotation.DeleteMapping
 *  org.springframework.web.bind.annotation.GetMapping
 *  org.springframework.web.bind.annotation.PathVariable
 *  org.springframework.web.bind.annotation.PostMapping
 *  org.springframework.web.bind.annotation.PutMapping
 *  org.springframework.web.bind.annotation.RequestBody
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RequestParam
 *  org.springframework.web.bind.annotation.RestController
 */
package com.vrd.vehicle.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vrd.common.result.Result;
import com.vrd.vehicle.dto.VehicleAlertLongTrendVO;
import com.vrd.vehicle.dto.VehicleDTO;
import com.vrd.vehicle.dto.VehicleDashboardStatsVO;
import com.vrd.vehicle.dto.VehicleEcuDTO;
import com.vrd.vehicle.dto.VehicleOnlineTrendVO;
import com.vrd.vehicle.entity.Vehicle;
import com.vrd.vehicle.entity.VehicleEcu;
import com.vrd.vehicle.service.VehicleService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 车辆管理接口（服务端口 9082，网关路由前缀 /api/vehicle）
 * <p>
 * 提供车辆档案 CRUD、车辆 ECU 信息管理、Dashboard 统计趋势、
 * 以及从 Kafka / 外部 API 同步车辆数据的能力。
 */
@RestController
@RequestMapping(value={"/vehicle"})
public class VehicleController {
    @Autowired
    private VehicleService vehicleService;

    /**
     * 获取车辆 Dashboard 统计汇总
     * <p>GET /vehicle/stats
     * <p>用于系统首页：车辆总数、在线数、告警数等概览指标。
     *
     * @return VehicleDashboardStatsVO 统计汇总数据
     */
    @GetMapping(value={"/stats"})
    public Result<VehicleDashboardStatsVO> stats() {
        return Result.success(this.vehicleService.getDashboardStats());
    }

    /**
     * 获取车辆在线数趋势
     * <p>GET /vehicle/stats/online-trend
     *
     * @param granularity 粒度，可选 hour / day / month，默认 hour
     * @return VehicleOnlineTrendVO 在线趋势时序数据
     */
    @GetMapping(value={"/stats/online-trend"})
    public Result<VehicleOnlineTrendVO> onlineTrend(@RequestParam(value="granularity", defaultValue="hour") String granularity) {
        return Result.success(this.vehicleService.getOnlineTrend(granularity));
    }

    /**
     * 获取告警/故障长期趋势
     * <p>GET /vehicle/stats/alert-long-trend
     *
     * @param granularity 粒度，可选 hour / day / month，默认 hour
     * @param metric      指标名，默认 faultCount（故障数）
     * @return VehicleAlertLongTrendVO 告警趋势时序数据
     */
    @GetMapping(value={"/stats/alert-long-trend"})
    public Result<VehicleAlertLongTrendVO> alertLongTrend(@RequestParam(value="granularity", defaultValue="hour") String granularity, @RequestParam(value="metric", defaultValue="faultCount") String metric) {
        return Result.success(this.vehicleService.getAlertLongTrend(granularity, metric));
    }

    /**
     * 分页查询车辆列表
     * <p>GET /vehicle/page
     *
     * @param current 当前页码，默认 1
     * @param size    每页条数，默认 10
     * @param keyword 可选，按 VIN / 车牌号模糊过滤
     * @param modelId 可选，按车型 ID 过滤
     * @return 分页结果 Page&lt;Vehicle&gt;
     */
    @GetMapping(value={"/page"})
    public Result<Page<Vehicle>> page(@RequestParam(value="current", defaultValue="1") Integer current, @RequestParam(value="size", defaultValue="10") Integer size, @RequestParam(value="keyword", required=false) String keyword, @RequestParam(value="modelId", required=false) Long modelId) {
        Page<Vehicle> page = this.vehicleService.page(current, size, keyword, modelId);
        return Result.success(page);
    }

    /**
     * 按 ID 查询车辆详情
     * <p>GET /vehicle/{id}
     *
     * @param id 车辆 ID
     * @return Vehicle 车辆档案；不存在时 data 为 null
     */
    @GetMapping(value={"/{id}"})
    public Result<Vehicle> getById(@PathVariable(value="id") Long id) {
        Vehicle vehicle = this.vehicleService.getById(id);
        return Result.success(vehicle);
    }

    /**
     * 按 VIN 查询车辆详情
     * <p>GET /vehicle/vin/{vin}
     *
     * @param vin 车架号（17 位 VIN 码）
     * @return Vehicle 车辆档案；不存在时 data 为 null
     */
    @GetMapping(value={"/vin/{vin}"})
    public Result<Vehicle> getByVin(@PathVariable(value="vin") String vin) {
        Vehicle vehicle = this.vehicleService.lambdaQuery().eq(Vehicle::getVin, vin).one();
        return Result.success(vehicle);
    }

    /**
     * 按车型分页查询车辆 ID + VIN 列表
     * <p>GET /vehicle/list-by-model?modelId={modelId}
     * <p>用于 DBC 按车型下发时批量获取目标车辆，仅返回 id 和 vin 字段，
     * 避免传输完整 Vehicle 对象。
     *
     * @param modelId  车型 ID（必填）
     * @param current  当前页码，默认 1
     * @param size     每页条数，默认 500
     * @return List&lt;Map&gt; 每项含 id 和 vin
     */
    @GetMapping(value={"/list-by-model"})
    public Result<List<Map<String, Object>>> listByModel(
            @RequestParam(value="modelId") Long modelId,
            @RequestParam(value="current", defaultValue="1") Integer current,
            @RequestParam(value="size", defaultValue="500") Integer size) {
        Page<Vehicle> page = new Page<>(current, size);
        page.setSearchCount(false);
        LambdaQueryWrapper<Vehicle> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Vehicle::getModelId, modelId)
               .eq(Vehicle::getDeleted, 0)
               .select(Vehicle::getId, Vehicle::getVin);
        this.vehicleService.page(page, wrapper);
        List<Map<String, Object>> list = page.getRecords().stream().map(v -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", v.getId());
            m.put("vin", v.getVin());
            return m;
        }).collect(Collectors.toList());
        return Result.success(list);
    }

    /**
     * 新增车辆档案
     * <p>POST /vehicle
     *
     * @param dto 车辆信息（VIN、车型、车牌、颜色、生产年份、发动机号、车身号、配置字、ECU 版本等）
     * @return 创建成功后的 Vehicle（含生成的 ID）
     */
    @PostMapping
    public Result<Vehicle> create(@RequestBody VehicleDTO dto) {
        Vehicle vehicle = new Vehicle();
        vehicle.setVin(dto.getVin());
        vehicle.setModelId(dto.getModelId());
        vehicle.setPlateNumber(dto.getPlateNumber());
        vehicle.setColor(dto.getColor());
        vehicle.setProductionYear(dto.getProductionYear());
        vehicle.setEngineNumber(dto.getEngineNumber());
        vehicle.setBodyNumber(dto.getBodyNumber());
        vehicle.setConfigWord(dto.getConfigWord());
        vehicle.setCurrentEcuVersion(dto.getCurrentEcuVersion());
        Vehicle result = this.vehicleService.create(vehicle);
        return Result.success(result);
    }

    /**
     * 更新车辆档案
     * <p>PUT /vehicle/{id}
     *
     * @param id  车辆 ID
     * @param dto 待更新的车辆字段
     * @return 更新后的 Vehicle
     */
    @PutMapping(value={"/{id}"})
    public Result<Vehicle> update(@PathVariable(value="id") Long id, @RequestBody VehicleDTO dto) {
        Vehicle vehicle = new Vehicle();
        vehicle.setId(id);
        vehicle.setVin(dto.getVin());
        vehicle.setModelId(dto.getModelId());
        vehicle.setPlateNumber(dto.getPlateNumber());
        vehicle.setColor(dto.getColor());
        vehicle.setProductionYear(dto.getProductionYear());
        vehicle.setEngineNumber(dto.getEngineNumber());
        vehicle.setBodyNumber(dto.getBodyNumber());
        vehicle.setConfigWord(dto.getConfigWord());
        vehicle.setCurrentEcuVersion(dto.getCurrentEcuVersion());
        Vehicle result = this.vehicleService.update(vehicle);
        return Result.success(result);
    }

    /**
     * 删除车辆（逻辑删除，置 deleted=1）
     * <p>DELETE /vehicle/{id}
     *
     * @param id 车辆 ID
     * @return 空结果；车辆不存在时静默成功
     */
    @DeleteMapping(value={"/{id}"})
    public Result<Void> delete(@PathVariable(value="id") Long id) {
        Vehicle vehicle = this.vehicleService.getById(id);
        if (vehicle != null) {
            vehicle.setDeleted(1);
            this.vehicleService.updateById(vehicle);
        }
        return Result.success();
    }

    /**
     * 触发从 Kafka 同步车辆数据
     * <p>POST /vehicle/sync/kafka
     * <p>手动触发消费车辆同步 Topic，异步执行，接口立即返回。
     *
     * @return 空结果
     */
    @PostMapping(value={"/sync/kafka"})
    public Result<Void> syncFromKafka() {
        this.vehicleService.syncFromKafka();
        return Result.success();
    }

    /**
     * 触发从外部 API 同步车辆数据
     * <p>POST /vehicle/sync/api?apiUrl={url}
     *
     * @param apiUrl 外部数据源接口地址
     * @return 空结果
     */
    @PostMapping(value={"/sync/api"})
    public Result<Void> syncFromApi(@RequestParam(value="apiUrl") String apiUrl) {
        this.vehicleService.syncFromApi(apiUrl);
        return Result.success();
    }

    /**
     * 查询车辆下的 ECU 列表
     * <p>GET /vehicle/{id}/ecu
     *
     * @param id 车辆 ID
     * @return List&lt;VehicleEcu&gt; 该车全部 ECU 信息
     */
    @GetMapping(value={"/{id}/ecu"})
    public Result<List<VehicleEcu>> getEcus(@PathVariable(value="id") Long id) {
        List<VehicleEcu> ecus = this.vehicleService.getEcusByVehicleId(id);
        return Result.success(ecus);
    }

    /**
     * 为车辆新增 ECU 记录
     * <p>POST /vehicle/{id}/ecu
     *
     * @param id  车辆 ID
     * @param dto ECU 信息（ecuType、零件号、软硬件版本、供应商、序列号、安装日期）
     * @return 空结果（新增记录状态默认启用）
     */
    @PostMapping(value={"/{id}/ecu"})
    public Result<Void> addEcu(@PathVariable(value="id") Long id, @RequestBody VehicleEcuDTO dto) {
        VehicleEcu ecu = new VehicleEcu();
        ecu.setVehicleId(id);
        ecu.setEcuType(dto.getEcuType());
        ecu.setEcuPartNumber(dto.getEcuPartNumber());
        ecu.setHardwareVersion(dto.getHardwareVersion());
        ecu.setSoftwareVersion(dto.getSoftwareVersion());
        ecu.setSupplier(dto.getSupplier());
        ecu.setSerialNumber(dto.getSerialNumber());
        if (dto.getInstallDate() != null) {
            ecu.setInstallDate(dto.getInstallDate().atStartOfDay());
        }
        ecu.setStatus(1);
        this.vehicleService.addEcu(ecu);
        return Result.success();
    }

    /**
     * 更新车辆 ECU 信息
     * <p>PUT /vehicle/ecu/{ecuId}
     *
     * @param ecuId ECU 记录 ID
     * @param dto   待更新的 ECU 字段
     * @return 空结果
     */
    @PutMapping(value={"/ecu/{ecuId}"})
    public Result<Void> updateEcu(@PathVariable(value="ecuId") Long ecuId, @RequestBody VehicleEcuDTO dto) {
        VehicleEcu ecu = new VehicleEcu();
        ecu.setId(ecuId);
        ecu.setEcuType(dto.getEcuType());
        ecu.setEcuPartNumber(dto.getEcuPartNumber());
        ecu.setHardwareVersion(dto.getHardwareVersion());
        ecu.setSoftwareVersion(dto.getSoftwareVersion());
        ecu.setSupplier(dto.getSupplier());
        ecu.setSerialNumber(dto.getSerialNumber());
        if (dto.getInstallDate() != null) {
            ecu.setInstallDate(dto.getInstallDate().atStartOfDay());
        }
        this.vehicleService.updateEcu(ecu);
        return Result.success();
    }
}

