/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.alibaba.fastjson2.JSON
 *  com.alibaba.fastjson2.JSONObject
 *  com.alibaba.fastjson2.JSONReader$Feature
 *  com.alibaba.fastjson2.JSONWriter$Feature
 *  com.baomidou.mybatisplus.core.conditions.Wrapper
 *  com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper
 *  com.baomidou.mybatisplus.core.metadata.IPage
 *  com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper
 *  com.baomidou.mybatisplus.extension.plugins.pagination.Page
 *  com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
 *  com.vrd.common.exception.BusinessException
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.http.MediaType
 *  org.springframework.kafka.core.KafkaTemplate
 *  org.springframework.scheduling.annotation.Async
 *  org.springframework.stereotype.Service
 *  org.springframework.web.reactive.function.client.WebClient
 */
package com.vrd.vehicle.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vrd.common.exception.BusinessException;
import com.vrd.vehicle.config.VehicleKafkaProperties;
import com.vrd.vehicle.dto.VehicleAlertLongTrendVO;
import com.vrd.vehicle.dto.VehicleDashboardStatsVO;
import com.vrd.vehicle.dto.VehicleOnlineTrendVO;
import com.vrd.vehicle.entity.SyncLog;
import com.vrd.vehicle.entity.Vehicle;
import com.vrd.vehicle.entity.VehicleAlert;
import com.vrd.vehicle.entity.VehicleEcu;
import com.vrd.vehicle.entity.VehicleFault;
import com.vrd.vehicle.entity.VehicleModel;
import com.vrd.vehicle.mapper.SyncLogMapper;
import com.vrd.vehicle.mapper.VehicleAlertMapper;
import com.vrd.vehicle.mapper.VehicleAlertTrendStatMapper;
import com.vrd.vehicle.mapper.VehicleEcuMapper;
import com.vrd.vehicle.mapper.VehicleFaultMapper;
import com.vrd.vehicle.mapper.VehicleMapper;
import com.vrd.vehicle.mapper.VehicleOnlineStatMapper;
import com.vrd.vehicle.service.VehicleModelService;
import com.vrd.vehicle.service.VehicleService;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class VehicleServiceImpl
extends ServiceImpl<VehicleMapper, Vehicle>
implements VehicleService {
    private static final Logger log = LoggerFactory.getLogger(VehicleServiceImpl.class);
    @Autowired
    private VehicleEcuMapper vehicleEcuMapper;
    @Autowired
    private SyncLogMapper syncLogMapper;
    @Autowired
    private VehicleModelService vehicleModelService;
    @Autowired
    private VehicleAlertMapper vehicleAlertMapper;
    @Autowired
    private VehicleFaultMapper vehicleFaultMapper;
    @Autowired
    private VehicleOnlineStatMapper vehicleOnlineStatMapper;
    @Autowired
    private VehicleAlertTrendStatMapper vehicleAlertTrendStatMapper;
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    private VehicleKafkaProperties vehicleKafkaProperties;
    private static final DateTimeFormatter HOUR_LABEL_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DAY_LABEL_FORMAT = DateTimeFormatter.ofPattern("MM-dd");
    private static final DateTimeFormatter WEEK_LABEL_FORMAT = DateTimeFormatter.ofPattern("MM-dd");
    private static final DateTimeFormatter MONTH_LABEL_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final DateTimeFormatter ALERT_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public VehicleDashboardStatsVO getDashboardStats() {
        VehicleDashboardStatsVO stats = new VehicleDashboardStatsVO();
        long connectedModelCount = ((LambdaQueryChainWrapper)((LambdaQueryChainWrapper)this.vehicleModelService.lambdaQuery().eq(VehicleModel::getDeleted, (Object)0)).eq(VehicleModel::getStatus, (Object)1)).count();
        stats.setConnectedModelCount(connectedModelCount);
        long totalVehicles = ((LambdaQueryChainWrapper)this.lambdaQuery().eq(Vehicle::getDeleted, (Object)0)).count();
        stats.setTotalVehicles(totalVehicles);
        long onlineVehicles = ((LambdaQueryChainWrapper)((LambdaQueryChainWrapper)this.lambdaQuery().eq(Vehicle::getDeleted, (Object)0)).eq(Vehicle::getStatus, (Object)1)).count();
        stats.setOnlineVehicles(onlineVehicles);
        List models = ((LambdaQueryChainWrapper)((LambdaQueryChainWrapper)this.vehicleModelService.lambdaQuery().eq(VehicleModel::getDeleted, (Object)0)).orderByAsc(VehicleModel::getModelName)).list();
        List vehicles = ((LambdaQueryChainWrapper)((LambdaQueryChainWrapper)this.lambdaQuery().eq(Vehicle::getDeleted, (Object)0)).isNotNull(Vehicle::getModelId)).list();
        Map<Long, Long> countByModelId = vehicles.stream().collect(Collectors.groupingBy(Vehicle::getModelId, Collectors.counting()));
        ArrayList<VehicleDashboardStatsVO.ModelVehicleStat> modelStats = new ArrayList<VehicleDashboardStatsVO.ModelVehicleStat>();
        for (VehicleModel model : models) {
            VehicleDashboardStatsVO.ModelVehicleStat item = new VehicleDashboardStatsVO.ModelVehicleStat();
            item.setModelId(model.getId());
            item.setModelName(model.getModelName());
            item.setModelCode(model.getModelCode());
            item.setVehicleCount(countByModelId.getOrDefault(model.getId(), 0L));
            modelStats.add(item);
        }
        stats.setModelStats(modelStats);
        stats.setTotalAlertCount(this.vehicleAlertMapper.selectCount((Wrapper)new LambdaQueryWrapper().eq(VehicleAlert::getDeleted, (Object)0)));
        stats.setAlertByComponent(this.buildAlertByComponent());
        stats.setRecentAlerts(this.buildRecentAlerts());
        stats.setTotalFaultCount(this.vehicleFaultMapper.selectCount((Wrapper)new LambdaQueryWrapper().eq(VehicleFault::getDeleted, (Object)0)));
        stats.setFaultByCode(this.buildFaultByCode());
        return stats;
    }

    @Override
    public VehicleOnlineTrendVO getOnlineTrend(String granularity) {
        String mode = "day".equalsIgnoreCase(granularity) ? "day" : "hour";
        List<Map<String, Object>> rows = "day".equals(mode) ? this.vehicleOnlineStatMapper.listDailyTrend() : this.vehicleOnlineStatMapper.listHourlyTrend();
        VehicleOnlineTrendVO trend = new VehicleOnlineTrendVO();
        trend.setGranularity(mode);
        ArrayList<VehicleOnlineTrendVO.TrendPoint> points = new ArrayList<VehicleOnlineTrendVO.TrendPoint>();
        for (Map<String, Object> row : rows) {
            LocalDateTime statTime = this.toLocalDateTime(row.get("statTime"));
            if (statTime == null) continue;
            VehicleOnlineTrendVO.TrendPoint point = new VehicleOnlineTrendVO.TrendPoint();
            point.setTimeLabel("day".equals(mode) ? statTime.format(DAY_LABEL_FORMAT) : statTime.format(HOUR_LABEL_FORMAT));
            Object count = row.get("onlineCount");
            point.setOnlineCount(count instanceof Number ? ((Number)count).longValue() : Long.parseLong(String.valueOf(count)));
            points.add(point);
        }
        trend.setPoints(points);
        return trend;
    }

    @Override
    public VehicleAlertLongTrendVO getAlertLongTrend(String granularity, String metric) {
        String mode = this.normalizeGranularity(granularity);
        String metricMode = this.normalizeMetric(metric);
        List<Map<String, Object>> rows = this.vehicleAlertTrendStatMapper.listTrend(mode);
        VehicleAlertLongTrendVO trend = new VehicleAlertLongTrendVO();
        trend.setGranularity(mode);
        trend.setMetric(metricMode);
        ArrayList<VehicleAlertLongTrendVO.TrendPoint> points = new ArrayList<VehicleAlertLongTrendVO.TrendPoint>();
        for (Map<String, Object> row : rows) {
            LocalDateTime statTime = this.toLocalDateTime(row.get("statTime"));
            if (statTime == null) continue;
            long faultCount = this.toLong(row.get("faultCount"));
            long faultVehicleCount = this.toLong(row.get("faultVehicleCount"));
            VehicleAlertLongTrendVO.TrendPoint point = new VehicleAlertLongTrendVO.TrendPoint();
            point.setTimeLabel(this.formatTrendLabel(mode, statTime));
            point.setFaultCount(faultCount);
            point.setFaultVehicleCount(faultVehicleCount);
            point.setValue(this.resolveMetricValue(metricMode, faultCount, faultVehicleCount));
            points.add(point);
        }
        trend.setPoints(points);
        return trend;
    }

    private String normalizeGranularity(String granularity) {
        if (granularity == null) {
            return "hour";
        }
        return switch (granularity.toLowerCase()) {
            case "day", "week", "month" -> granularity.toLowerCase();
            default -> "hour";
        };
    }

    private String normalizeMetric(String metric) {
        if (metric == null) {
            return "faultCount";
        }
        return switch (metric) {
            case "faultVehicleCount", "avgFaultPerVehicle" -> metric;
            default -> "faultCount";
        };
    }

    private String formatTrendLabel(String mode, LocalDateTime statTime) {
        return switch (mode) {
            case "day" -> statTime.format(DAY_LABEL_FORMAT);
            case "week" -> statTime.format(WEEK_LABEL_FORMAT);
            case "month" -> statTime.format(MONTH_LABEL_FORMAT);
            default -> statTime.format(HOUR_LABEL_FORMAT);
        };
    }

    private Double resolveMetricValue(String metricMode, long faultCount, long faultVehicleCount) {
        return switch (metricMode) {
            case "faultVehicleCount" -> faultVehicleCount;
            case "avgFaultPerVehicle" -> faultVehicleCount > 0L ? (double)Math.round((double)faultCount * 100.0 / (double)faultVehicleCount) / 100.0 : 0.0;
            default -> faultCount;
        };
    }

    private long toLong(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number) {
            return ((Number)value).longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }

    private LocalDateTime toLocalDateTime(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDateTime) {
            return (LocalDateTime)value;
        }
        if (value instanceof Timestamp) {
            return ((Timestamp)value).toLocalDateTime();
        }
        if (value instanceof Date) {
            return LocalDateTime.ofInstant(((Date)value).toInstant(), ZoneId.systemDefault());
        }
        return LocalDateTime.parse(String.valueOf(value).replace(' ', 'T'));
    }

    private List<VehicleDashboardStatsVO.FaultByCode> buildFaultByCode() {
        List<Map<String, Object>> rows = this.vehicleFaultMapper.countGroupByFaultCode();
        ArrayList<VehicleDashboardStatsVO.FaultByCode> result = new ArrayList<VehicleDashboardStatsVO.FaultByCode>();
        for (Map<String, Object> row : rows) {
            VehicleDashboardStatsVO.FaultByCode item = new VehicleDashboardStatsVO.FaultByCode();
            item.setFaultCode(String.valueOf(row.get("faultCode")));
            Object componentCode = row.get("componentCode");
            item.setComponentCode(componentCode != null ? String.valueOf(componentCode) : null);
            Object faultName = row.get("faultName");
            item.setFaultName(faultName != null ? String.valueOf(faultName) : item.getFaultCode());
            Object count = row.get("faultCount");
            item.setFaultCount(count instanceof Number ? ((Number)count).longValue() : Long.parseLong(String.valueOf(count)));
            result.add(item);
        }
        return result;
    }

    private List<VehicleDashboardStatsVO.AlertByComponent> buildAlertByComponent() {
        List<Map<String, Object>> rows = this.vehicleAlertMapper.countGroupByComponent();
        ArrayList<VehicleDashboardStatsVO.AlertByComponent> result = new ArrayList<VehicleDashboardStatsVO.AlertByComponent>();
        for (Map<String, Object> row : rows) {
            VehicleDashboardStatsVO.AlertByComponent item = new VehicleDashboardStatsVO.AlertByComponent();
            item.setComponentCode(String.valueOf(row.get("componentCode")));
            Object count = row.get("alertCount");
            item.setAlertCount(count instanceof Number ? ((Number)count).longValue() : Long.parseLong(String.valueOf(count)));
            result.add(item);
        }
        return result;
    }

    private List<VehicleDashboardStatsVO.RecentAlert> buildRecentAlerts() {
        List alerts = this.vehicleAlertMapper.selectList((Wrapper)((LambdaQueryWrapper)((LambdaQueryWrapper)new LambdaQueryWrapper().eq(VehicleAlert::getDeleted, (Object)0)).orderByDesc(VehicleAlert::getAlertTime)).last("LIMIT 10"));
        ArrayList<VehicleDashboardStatsVO.RecentAlert> result = new ArrayList<VehicleDashboardStatsVO.RecentAlert>();
        for (VehicleAlert alert : alerts) {
            VehicleDashboardStatsVO.RecentAlert item = new VehicleDashboardStatsVO.RecentAlert();
            item.setTime(alert.getAlertTime() != null ? alert.getAlertTime().format(ALERT_TIME_FORMAT) : "");
            item.setVin(alert.getVin());
            item.setComponentCode(this.resolveComponentCode(alert));
            item.setType(alert.getAlertType());
            item.setMessage(alert.getMessage());
            item.setStatus(alert.getStatus() != null && alert.getStatus() == 1 ? "\u5df2\u5904\u7406" : "\u672a\u5904\u7406");
            result.add(item);
        }
        return result;
    }

    private String resolveComponentCode(VehicleAlert alert) {
        if (alert.getComponentCode() != null && !alert.getComponentCode().isBlank()) {
            return alert.getComponentCode();
        }
        return alert.getEcuType();
    }

    @Override
    public Page<Vehicle> page(Integer current, Integer size, String keyword, Long modelId) {
        Page page = new Page((long)current.intValue(), (long)size.intValue());
        LambdaQueryWrapper wrapper = new LambdaQueryWrapper();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> ((LambdaQueryWrapper)((LambdaQueryWrapper)w.like(Vehicle::getVin, (Object)keyword)).or()).like(Vehicle::getPlateNumber, (Object)keyword));
        }
        if (modelId != null) {
            wrapper.eq(Vehicle::getModelId, (Object)modelId);
        }
        IPage result = this.page((IPage)page, (Wrapper)wrapper);
        return (Page)result;
    }

    @Override
    public Vehicle create(Vehicle vehicle) {
        if (vehicle.getVin() == null || vehicle.getVin().isEmpty()) {
            throw new BusinessException("VIN\u7801\u4e0d\u80fd\u4e3a\u7a7a");
        }
        Vehicle exist = (Vehicle)((LambdaQueryChainWrapper)this.lambdaQuery().eq(Vehicle::getVin, (Object)vehicle.getVin())).one();
        if (exist != null) {
            throw new BusinessException("\u8f66\u8f86VIN\u7801\u5df2\u5b58\u5728");
        }
        vehicle.setDataSource(1);
        vehicle.setStatus(1);
        vehicle.setDeleted(0);
        vehicle.setCreateTime(LocalDateTime.now());
        vehicle.setUpdateTime(LocalDateTime.now());
        this.save(vehicle);
        this.publishToKafka(vehicle);
        return vehicle;
    }

    @Override
    public Vehicle update(Vehicle vehicle) {
        if (vehicle.getId() == null) {
            throw new BusinessException("\u8f66\u8f86ID\u4e0d\u80fd\u4e3a\u7a7a");
        }
        Vehicle exist = (Vehicle)this.getById(vehicle.getId());
        if (exist == null) {
            throw new BusinessException("\u8f66\u8f86\u4e0d\u5b58\u5728");
        }
        vehicle.setUpdateTime(LocalDateTime.now());
        this.updateById(vehicle);
        this.publishToKafka(vehicle);
        return vehicle;
    }

    @Override
    public void syncFromKafka() {
        log.info("\u8f66\u8f86 Kafka \u540c\u6b65\u5df2\u542f\u7528\uff0c\u6d88\u8d39\u4e3b\u9898: {}", (Object)this.vehicleKafkaProperties.getConsumerTopic());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void processKafkaMessage(String message) {
        SyncLog syncLog = new SyncLog();
        syncLog.setSyncType("KAFKA");
        syncLog.setSource(this.vehicleKafkaProperties.getConsumerTopic());
        syncLog.setTarget("database");
        syncLog.setStartTime(LocalDateTime.now());
        syncLog.setCreateTime(LocalDateTime.now());
        syncLog.setStatus("PROCESSING");
        syncLog.setPayload(message);
        syncLog.setRecordCount(0);
        try {
            JSONObject jsonObject = JSON.parseObject((String)message);
            String action = jsonObject.getString("action");
            JSONObject data = jsonObject.getJSONObject("data");
            syncLog.setAction(action);
            if (data == null) {
                throw new BusinessException("\u540c\u6b65\u6570\u636e\u7f3a\u5c11 data \u5b57\u6bb5");
            }
            if ("CREATE".equals(action)) {
                Vehicle vehicle = (Vehicle)data.toJavaObject(Vehicle.class, new JSONReader.Feature[0]);
                if (vehicle.getVin() == null || vehicle.getVin().isBlank()) {
                    throw new BusinessException("VIN\u7801\u4e0d\u80fd\u4e3a\u7a7a");
                }
                syncLog.setVin(vehicle.getVin());
                vehicle.setDataSource(2);
                vehicle.setDeleted(0);
                vehicle.setCreateTime(LocalDateTime.now());
                vehicle.setUpdateTime(LocalDateTime.now());
                this.save(vehicle);
                syncLog.setRecordCount(1);
            } else if ("UPDATE".equals(action)) {
                Vehicle vehicle = (Vehicle)data.toJavaObject(Vehicle.class, new JSONReader.Feature[0]);
                syncLog.setVin(vehicle.getVin());
                vehicle.setUpdateTime(LocalDateTime.now());
                this.updateById(vehicle);
                syncLog.setRecordCount(1);
            } else {
                throw new BusinessException("\u4e0d\u652f\u6301\u7684\u540c\u6b65\u52a8\u4f5c: " + action);
            }
            syncLog.setStatus("SUCCESS");
        }
        catch (Exception e) {
            syncLog.setStatus("FAILED");
            syncLog.setMessage(e.getMessage());
            log.warn("\u5904\u7406 Kafka \u8f66\u8f86\u540c\u6b65\u6d88\u606f\u5931\u8d25: {}", (Object)e.getMessage());
        }
        finally {
            syncLog.setEndTime(LocalDateTime.now());
            this.syncLogMapper.insert(syncLog);
        }
    }

    @Override
    @Async
    public void syncFromApi(String apiUrl) {
        SyncLog syncLog = new SyncLog();
        syncLog.setSyncType("API");
        syncLog.setSource(apiUrl);
        syncLog.setTarget("database");
        syncLog.setAction("BATCH");
        syncLog.setStartTime(LocalDateTime.now());
        syncLog.setStatus("PROCESSING");
        syncLog.setCreateTime(LocalDateTime.now());
        syncLog.setRecordCount(0);
        String response = null;
        try {
            response = (String)WebClient.create((String)apiUrl).get().accept(new MediaType[]{MediaType.APPLICATION_JSON}).retrieve().bodyToMono(String.class).block();
            syncLog.setPayload(response);
            List vehicles = JSON.parseArray((String)response, Vehicle.class);
            for (Vehicle vehicle : vehicles) {
                Vehicle exist = (Vehicle)((LambdaQueryChainWrapper)this.lambdaQuery().eq(Vehicle::getVin, (Object)vehicle.getVin())).one();
                if (exist == null) {
                    vehicle.setDataSource(3);
                    vehicle.setDeleted(0);
                    vehicle.setCreateTime(LocalDateTime.now());
                    vehicle.setUpdateTime(LocalDateTime.now());
                    this.save(vehicle);
                    continue;
                }
                vehicle.setId(exist.getId());
                vehicle.setUpdateTime(LocalDateTime.now());
                this.updateById(vehicle);
            }
            syncLog.setRecordCount(vehicles.size());
            syncLog.setStatus("SUCCESS");
        }
        catch (Exception e) {
            syncLog.setStatus("FAILED");
            syncLog.setMessage(e.getMessage());
            if (response != null) {
                syncLog.setPayload(response);
            }
            log.warn("API \u8f66\u8f86\u540c\u6b65\u5931\u8d25: {}", (Object)e.getMessage());
        }
        syncLog.setEndTime(LocalDateTime.now());
        this.syncLogMapper.insert(syncLog);
    }

    private void publishToKafka(Vehicle vehicle) {
        JSONObject message = new JSONObject();
        message.put((Object)"action", (Object)(vehicle.getId() == null ? "CREATE" : "UPDATE"));
        message.put((Object)"data", JSON.toJSON((Object)vehicle));
        this.kafkaTemplate.send(this.vehicleKafkaProperties.getProducerTopic(), (Object)vehicle.getVin(), (Object)message.toJSONString(new JSONWriter.Feature[0]));
    }

    @Override
    public void addEcu(VehicleEcu ecu) {
        ecu.setDeleted(0);
        ecu.setCreateTime(LocalDateTime.now());
        ecu.setUpdateTime(LocalDateTime.now());
        this.vehicleEcuMapper.insert(ecu);
    }

    @Override
    public void updateEcu(VehicleEcu ecu) {
        ecu.setUpdateTime(LocalDateTime.now());
        this.vehicleEcuMapper.updateById(ecu);
    }

    @Override
    public List<VehicleEcu> getEcusByVehicleId(Long vehicleId) {
        return this.vehicleEcuMapper.selectList((Wrapper)((LambdaQueryWrapper)new LambdaQueryWrapper().eq(VehicleEcu::getVehicleId, (Object)vehicleId)).eq(VehicleEcu::getDeleted, (Object)0));
    }
}

