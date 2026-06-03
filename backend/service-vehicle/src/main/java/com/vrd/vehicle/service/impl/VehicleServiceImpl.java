package com.vrd.vehicle.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vrd.common.exception.BusinessException;
import com.vrd.vehicle.dto.VehicleAlertLongTrendVO;
import com.vrd.vehicle.dto.VehicleDashboardStatsVO;
import com.vrd.vehicle.dto.VehicleOnlineTrendVO;
import com.vrd.vehicle.entity.SyncLog;
import com.vrd.vehicle.entity.Vehicle;
import com.vrd.vehicle.entity.VehicleEcu;
import com.vrd.vehicle.entity.VehicleModel;
import com.vrd.vehicle.mapper.SyncLogMapper;
import com.vrd.vehicle.mapper.VehicleAlertMapper;
import com.vrd.vehicle.mapper.VehicleAlertTrendStatMapper;
import com.vrd.vehicle.mapper.VehicleFaultMapper;
import com.vrd.vehicle.mapper.VehicleOnlineStatMapper;
import com.vrd.vehicle.mapper.VehicleEcuMapper;
import com.vrd.vehicle.mapper.VehicleMapper;
import com.vrd.vehicle.entity.VehicleAlert;
import com.vrd.vehicle.entity.VehicleFault;
import com.vrd.vehicle.service.VehicleModelService;
import com.vrd.vehicle.service.VehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class VehicleServiceImpl extends ServiceImpl<VehicleMapper, Vehicle> implements VehicleService {

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

    private static final DateTimeFormatter HOUR_LABEL_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DAY_LABEL_FORMAT = DateTimeFormatter.ofPattern("MM-dd");
    private static final DateTimeFormatter WEEK_LABEL_FORMAT = DateTimeFormatter.ofPattern("MM-dd");
    private static final DateTimeFormatter MONTH_LABEL_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final DateTimeFormatter ALERT_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final String VEHICLE_TOPIC = "vehicle-data";

    @Override
    public VehicleDashboardStatsVO getDashboardStats() {
        VehicleDashboardStatsVO stats = new VehicleDashboardStatsVO();

        long connectedModelCount = vehicleModelService.lambdaQuery()
                .eq(VehicleModel::getDeleted, 0)
                .eq(VehicleModel::getStatus, 1)
                .count();
        stats.setConnectedModelCount(connectedModelCount);

        long totalVehicles = lambdaQuery().eq(Vehicle::getDeleted, 0).count();
        stats.setTotalVehicles(totalVehicles);

        long onlineVehicles = lambdaQuery()
                .eq(Vehicle::getDeleted, 0)
                .eq(Vehicle::getStatus, 1)
                .count();
        stats.setOnlineVehicles(onlineVehicles);

        List<VehicleModel> models = vehicleModelService.lambdaQuery()
                .eq(VehicleModel::getDeleted, 0)
                .orderByAsc(VehicleModel::getModelName)
                .list();
        List<Vehicle> vehicles = lambdaQuery()
                .eq(Vehicle::getDeleted, 0)
                .isNotNull(Vehicle::getModelId)
                .list();
        Map<Long, Long> countByModelId = vehicles.stream()
                .collect(Collectors.groupingBy(Vehicle::getModelId, Collectors.counting()));

        List<VehicleDashboardStatsVO.ModelVehicleStat> modelStats = new ArrayList<>();
        for (VehicleModel model : models) {
            VehicleDashboardStatsVO.ModelVehicleStat item = new VehicleDashboardStatsVO.ModelVehicleStat();
            item.setModelId(model.getId());
            item.setModelName(model.getModelName());
            item.setModelCode(model.getModelCode());
            item.setVehicleCount(countByModelId.getOrDefault(model.getId(), 0L));
            modelStats.add(item);
        }
        stats.setModelStats(modelStats);
        stats.setTotalAlertCount(vehicleAlertMapper.selectCount(
                new LambdaQueryWrapper<VehicleAlert>().eq(VehicleAlert::getDeleted, 0)));
        stats.setAlertByComponent(buildAlertByComponent());
        stats.setRecentAlerts(buildRecentAlerts());
        stats.setTotalFaultCount(vehicleFaultMapper.selectCount(
                new LambdaQueryWrapper<VehicleFault>().eq(VehicleFault::getDeleted, 0)));
        stats.setFaultByCode(buildFaultByCode());
        return stats;
    }

    @Override
    public VehicleOnlineTrendVO getOnlineTrend(String granularity) {
        String mode = "day".equalsIgnoreCase(granularity) ? "day" : "hour";
        List<Map<String, Object>> rows = "day".equals(mode)
                ? vehicleOnlineStatMapper.listDailyTrend()
                : vehicleOnlineStatMapper.listHourlyTrend();

        VehicleOnlineTrendVO trend = new VehicleOnlineTrendVO();
        trend.setGranularity(mode);
        List<VehicleOnlineTrendVO.TrendPoint> points = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            LocalDateTime statTime = toLocalDateTime(row.get("statTime"));
            if (statTime == null) {
                continue;
            }
            VehicleOnlineTrendVO.TrendPoint point = new VehicleOnlineTrendVO.TrendPoint();
            point.setTimeLabel("day".equals(mode)
                    ? statTime.format(DAY_LABEL_FORMAT)
                    : statTime.format(HOUR_LABEL_FORMAT));
            Object count = row.get("onlineCount");
            point.setOnlineCount(count instanceof Number ? ((Number) count).longValue() : Long.parseLong(String.valueOf(count)));
            points.add(point);
        }
        trend.setPoints(points);
        return trend;
    }

    @Override
    public VehicleAlertLongTrendVO getAlertLongTrend(String granularity, String metric) {
        String mode = normalizeGranularity(granularity);
        String metricMode = normalizeMetric(metric);
        List<Map<String, Object>> rows = vehicleAlertTrendStatMapper.listTrend(mode);

        VehicleAlertLongTrendVO trend = new VehicleAlertLongTrendVO();
        trend.setGranularity(mode);
        trend.setMetric(metricMode);
        List<VehicleAlertLongTrendVO.TrendPoint> points = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            LocalDateTime statTime = toLocalDateTime(row.get("statTime"));
            if (statTime == null) {
                continue;
            }
            long faultCount = toLong(row.get("faultCount"));
            long faultVehicleCount = toLong(row.get("faultVehicleCount"));

            VehicleAlertLongTrendVO.TrendPoint point = new VehicleAlertLongTrendVO.TrendPoint();
            point.setTimeLabel(formatTrendLabel(mode, statTime));
            point.setFaultCount(faultCount);
            point.setFaultVehicleCount(faultVehicleCount);
            point.setValue(resolveMetricValue(metricMode, faultCount, faultVehicleCount));
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
            case "faultVehicleCount" -> (double) faultVehicleCount;
            case "avgFaultPerVehicle" -> faultVehicleCount > 0
                    ? Math.round(faultCount * 100.0 / faultVehicleCount) / 100.0
                    : 0.0;
            default -> (double) faultCount;
        };
    }

    private long toLong(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }

    private LocalDateTime toLocalDateTime(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDateTime) {
            return (LocalDateTime) value;
        }
        if (value instanceof java.sql.Timestamp) {
            return ((java.sql.Timestamp) value).toLocalDateTime();
        }
        if (value instanceof java.util.Date) {
            return LocalDateTime.ofInstant(((java.util.Date) value).toInstant(), java.time.ZoneId.systemDefault());
        }
        return LocalDateTime.parse(String.valueOf(value).replace(' ', 'T'));
    }

    private List<VehicleDashboardStatsVO.FaultByCode> buildFaultByCode() {
        List<Map<String, Object>> rows = vehicleFaultMapper.countGroupByFaultCode();
        List<VehicleDashboardStatsVO.FaultByCode> result = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            VehicleDashboardStatsVO.FaultByCode item = new VehicleDashboardStatsVO.FaultByCode();
            item.setFaultCode(String.valueOf(row.get("faultCode")));
            Object componentCode = row.get("componentCode");
            item.setComponentCode(componentCode != null ? String.valueOf(componentCode) : null);
            Object faultName = row.get("faultName");
            item.setFaultName(faultName != null ? String.valueOf(faultName) : item.getFaultCode());
            Object count = row.get("faultCount");
            item.setFaultCount(count instanceof Number ? ((Number) count).longValue() : Long.parseLong(String.valueOf(count)));
            result.add(item);
        }
        return result;
    }

    private List<VehicleDashboardStatsVO.AlertByComponent> buildAlertByComponent() {
        List<Map<String, Object>> rows = vehicleAlertMapper.countGroupByComponent();
        List<VehicleDashboardStatsVO.AlertByComponent> result = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            VehicleDashboardStatsVO.AlertByComponent item = new VehicleDashboardStatsVO.AlertByComponent();
            item.setComponentCode(String.valueOf(row.get("componentCode")));
            Object count = row.get("alertCount");
            item.setAlertCount(count instanceof Number ? ((Number) count).longValue() : Long.parseLong(String.valueOf(count)));
            result.add(item);
        }
        return result;
    }

    private List<VehicleDashboardStatsVO.RecentAlert> buildRecentAlerts() {
        List<VehicleAlert> alerts = vehicleAlertMapper.selectList(
                new LambdaQueryWrapper<VehicleAlert>()
                        .eq(VehicleAlert::getDeleted, 0)
                        .orderByDesc(VehicleAlert::getAlertTime)
                        .last("LIMIT 10"));
        List<VehicleDashboardStatsVO.RecentAlert> result = new ArrayList<>();
        for (VehicleAlert alert : alerts) {
            VehicleDashboardStatsVO.RecentAlert item = new VehicleDashboardStatsVO.RecentAlert();
            item.setTime(alert.getAlertTime() != null ? alert.getAlertTime().format(ALERT_TIME_FORMAT) : "");
            item.setVin(alert.getVin());
            item.setComponentCode(resolveComponentCode(alert));
            item.setType(alert.getAlertType());
            item.setMessage(alert.getMessage());
            item.setStatus(alert.getStatus() != null && alert.getStatus() == 1 ? "已处理" : "未处理");
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
        Page<Vehicle> page = new Page<>(current, size);
        LambdaQueryWrapper<Vehicle> wrapper = new LambdaQueryWrapper<>();
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Vehicle::getVin, keyword)
                    .or()
                    .like(Vehicle::getPlateNumber, keyword));
        }
        
        if (modelId != null) {
            wrapper.eq(Vehicle::getModelId, modelId);
        }
        
        IPage<Vehicle> result = page(page, wrapper);
        return (Page<Vehicle>) result;
    }

    @Override
    public Vehicle create(Vehicle vehicle) {
        if (vehicle.getVin() == null || vehicle.getVin().isEmpty()) {
            throw new BusinessException("VIN码不能为空");
        }
        
        Vehicle exist = lambdaQuery()
                .eq(Vehicle::getVin, vehicle.getVin())
                .one();
        
        if (exist != null) {
            throw new BusinessException("车辆VIN码已存在");
        }
        
        vehicle.setDataSource(1);
        vehicle.setStatus(1);
        vehicle.setDeleted(0);
        vehicle.setCreateTime(LocalDateTime.now());
        vehicle.setUpdateTime(LocalDateTime.now());
        
        save(vehicle);
        
        publishToKafka(vehicle);
        
        return vehicle;
    }

    @Override
    public Vehicle update(Vehicle vehicle) {
        if (vehicle.getId() == null) {
            throw new BusinessException("车辆ID不能为空");
        }
        
        Vehicle exist = getById(vehicle.getId());
        if (exist == null) {
            throw new BusinessException("车辆不存在");
        }
        
        vehicle.setUpdateTime(LocalDateTime.now());
        updateById(vehicle);
        
        publishToKafka(vehicle);
        
        return vehicle;
    }

    @Override
    @Async
    @KafkaListener(topics = VEHICLE_TOPIC, groupId = "vehicle-processor")
    public void syncFromKafka() {
    }

    public void processKafkaMessage(String message) {
        try {
            JSONObject jsonObject = JSON.parseObject(message);
            String action = jsonObject.getString("action");
            JSONObject data = jsonObject.getJSONObject("data");
            
            SyncLog syncLog = new SyncLog();
            syncLog.setSyncType("KAFKA");
            syncLog.setSource("kafka");
            syncLog.setTarget("database");
            syncLog.setStartTime(LocalDateTime.now());
            syncLog.setStatus("PROCESSING");
            
            if ("CREATE".equals(action)) {
                Vehicle vehicle = data.toJavaObject(Vehicle.class);
                vehicle.setDataSource(2);
                vehicle.setDeleted(0);
                vehicle.setCreateTime(LocalDateTime.now());
                vehicle.setUpdateTime(LocalDateTime.now());
                save(vehicle);
            } else if ("UPDATE".equals(action)) {
                Vehicle vehicle = data.toJavaObject(Vehicle.class);
                vehicle.setUpdateTime(LocalDateTime.now());
                updateById(vehicle);
            }
            
            syncLog.setStatus("SUCCESS");
            syncLog.setEndTime(LocalDateTime.now());
            syncLog.setCreateTime(LocalDateTime.now());
            syncLogMapper.insert(syncLog);
        } catch (Exception e) {
            throw new BusinessException("处理Kafka消息失败: " + e.getMessage());
        }
    }

    @Override
    @Async
    public void syncFromApi(String apiUrl) {
        SyncLog syncLog = new SyncLog();
        syncLog.setSyncType("API");
        syncLog.setSource(apiUrl);
        syncLog.setTarget("database");
        syncLog.setStartTime(LocalDateTime.now());
        syncLog.setStatus("PROCESSING");
        syncLog.setCreateTime(LocalDateTime.now());
        
        try {
            String response = WebClient.create(apiUrl)
                    .get()
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            
            List<Vehicle> vehicles = JSON.parseArray(response, Vehicle.class);
            
            for (Vehicle vehicle : vehicles) {
                Vehicle exist = lambdaQuery()
                        .eq(Vehicle::getVin, vehicle.getVin())
                        .one();
                
                if (exist == null) {
                    vehicle.setDataSource(3);
                    vehicle.setDeleted(0);
                    vehicle.setCreateTime(LocalDateTime.now());
                    vehicle.setUpdateTime(LocalDateTime.now());
                    save(vehicle);
                } else {
                    vehicle.setId(exist.getId());
                    vehicle.setUpdateTime(LocalDateTime.now());
                    updateById(vehicle);
                }
            }
            
            syncLog.setRecordCount(vehicles.size());
            syncLog.setStatus("SUCCESS");
        } catch (Exception e) {
            syncLog.setStatus("FAILED");
            syncLog.setMessage(e.getMessage());
        }
        
        syncLog.setEndTime(LocalDateTime.now());
        syncLogMapper.insert(syncLog);
    }

    private void publishToKafka(Vehicle vehicle) {
        JSONObject message = new JSONObject();
        message.put("action", vehicle.getId() == null ? "CREATE" : "UPDATE");
        message.put("data", JSON.toJSON(vehicle));
        
        kafkaTemplate.send(VEHICLE_TOPIC, vehicle.getVin(), message.toJSONString());
    }

    @Override
    public void addEcu(VehicleEcu ecu) {
        ecu.setDeleted(0);
        ecu.setCreateTime(LocalDateTime.now());
        ecu.setUpdateTime(LocalDateTime.now());
        vehicleEcuMapper.insert(ecu);
    }

    @Override
    public void updateEcu(VehicleEcu ecu) {
        ecu.setUpdateTime(LocalDateTime.now());
        vehicleEcuMapper.updateById(ecu);
    }

    @Override
    public List<VehicleEcu> getEcusByVehicleId(Long vehicleId) {
        return vehicleEcuMapper.selectList(
                new LambdaQueryWrapper<VehicleEcu>()
                        .eq(VehicleEcu::getVehicleId, vehicleId)
                        .eq(VehicleEcu::getDeleted, 0)
        );
    }
}
