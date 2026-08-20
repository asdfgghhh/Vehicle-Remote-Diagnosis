package com.vrd.vehicle.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vrd.vehicle.dto.FaultAnalysisDistributionVO;
import com.vrd.vehicle.dto.RiskVehicleVO;
import com.vrd.vehicle.dto.VehicleAlertLongTrendVO;
import com.vrd.vehicle.entity.FaultScene;
import com.vrd.vehicle.entity.Vehicle;
import com.vrd.vehicle.entity.VehicleFault;
import com.vrd.vehicle.entity.VehicleModel;
import com.vrd.vehicle.mapper.FaultSceneMapper;
import com.vrd.vehicle.mapper.VehicleAlertTrendStatMapper;
import com.vrd.vehicle.mapper.VehicleFaultMapper;
import com.vrd.vehicle.mapper.VehicleMapper;
import com.vrd.vehicle.mapper.VehicleModelMapper;
import com.vrd.vehicle.service.VehicleAnalysisService;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VehicleAnalysisServiceImpl implements VehicleAnalysisService {
    private static final DateTimeFormatter HOUR_LABEL_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DAY_LABEL_FORMAT = DateTimeFormatter.ofPattern("MM-dd");
    private static final DateTimeFormatter WEEK_LABEL_FORMAT = DateTimeFormatter.ofPattern("MM-dd");
    private static final DateTimeFormatter MONTH_LABEL_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");

    @Autowired
    private VehicleAlertTrendStatMapper vehicleAlertTrendStatMapper;
    @Autowired
    private VehicleFaultMapper vehicleFaultMapper;
    @Autowired
    private FaultSceneMapper faultSceneMapper;
    @Autowired
    private com.vrd.vehicle.mapper.FaultConfigMapper faultConfigMapper;
    @Autowired
    private VehicleMapper vehicleMapper;
    @Autowired
    private VehicleModelMapper vehicleModelMapper;

    @Override
    public VehicleAlertLongTrendVO getFaultTrend(String granularity) {
        String mode = this.normalizeGranularity(granularity);
        List<Map<String, Object>> rows = this.vehicleAlertTrendStatMapper.listTrend(mode);
        VehicleAlertLongTrendVO trend = new VehicleAlertLongTrendVO();
        trend.setGranularity(mode);
        trend.setMetric("faultCount");
        List<VehicleAlertLongTrendVO.TrendPoint> points = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            LocalDateTime statTime = this.toLocalDateTime(row.get("statTime"));
            if (statTime == null) continue;
            long faultCount = this.toLong(row.get("faultCount"));
            long faultVehicleCount = this.toLong(row.get("faultVehicleCount"));
            VehicleAlertLongTrendVO.TrendPoint point = new VehicleAlertLongTrendVO.TrendPoint();
            point.setTimeLabel(this.formatTrendLabel(mode, statTime));
            point.setFaultCount(faultCount);
            point.setFaultVehicleCount(faultVehicleCount);
            point.setValue((double) faultCount);
            points.add(point);
        }
        trend.setPoints(points);
        return trend;
    }

    @Override
    public FaultAnalysisDistributionVO getFaultDistribution() {
        FaultAnalysisDistributionVO vo = new FaultAnalysisDistributionVO();
        List<VehicleFault> faults = this.vehicleFaultMapper.selectList(
                new LambdaQueryWrapper<VehicleFault>().eq(VehicleFault::getDeleted, 0));
        if (faults == null) {
            faults = new ArrayList<>();
        }
        vo.setByLevel(this.distributeByLevel(faults));
        vo.setByDomain(this.distributeByDomain(faults));
        vo.setByScene(this.distributeByScene(faults));
        vo.setTopFaultCodes(this.topFaultCodes(faults));
        return vo;
    }

    private List<FaultAnalysisDistributionVO.CountItem> distributeByLevel(List<VehicleFault> faults) {
        List<com.vrd.vehicle.entity.FaultConfig> configs = this.faultConfigMapper.selectList(
                new LambdaQueryWrapper<com.vrd.vehicle.entity.FaultConfig>()
                        .eq(com.vrd.vehicle.entity.FaultConfig::getDeleted, 0));
        Map<String, Integer> codeLevel = new HashMap<>();
        for (com.vrd.vehicle.entity.FaultConfig config : configs) {
            if (config.getFaultCode() != null && config.getAlarmLevel() != null) {
                codeLevel.put(config.getFaultCode(), config.getAlarmLevel());
            }
        }
        Map<String, Long> countMap = new LinkedHashMap<>();
        for (VehicleFault fault : faults) {
            Integer level = codeLevel.get(fault.getFaultCode());
            String key = level == null ? "未定级" : level == 1 ? "一级(提示)" : level == 2 ? "二级(一般)" : level == 3 ? "三级(严重)" : "四级(致命)";
            countMap.merge(key, 1L, Long::sum);
        }
        return this.toCountItems(countMap);
    }

    private List<FaultAnalysisDistributionVO.CountItem> distributeByDomain(List<VehicleFault> faults) {
        Map<String, Long> countMap = faults.stream()
                .collect(Collectors.groupingBy(f -> f.getComponentCode() == null || f.getComponentCode().isBlank() ? "未标注" : f.getComponentCode(), Collectors.counting()));
        return this.toCountItems(countMap);
    }

    private List<FaultAnalysisDistributionVO.CountItem> distributeByScene(List<VehicleFault> faults) {
        List<FaultScene> scenes = this.faultSceneMapper.selectList(
                new LambdaQueryWrapper<FaultScene>().eq(FaultScene::getStatus, 1));
        Map<String, String> sceneByCode = new HashMap<>();
        for (FaultScene scene : scenes) {
            if (scene.getFaultCodes() == null) continue;
            for (String c : scene.getFaultCodes().split(",")) {
                sceneByCode.putIfAbsent(c.trim(), scene.getSceneName());
            }
        }
        Map<String, Long> countMap = new LinkedHashMap<>();
        for (VehicleFault fault : faults) {
            String scene = sceneByCode.get(fault.getFaultCode());
            countMap.merge(scene == null ? "未关联场景" : scene, 1L, Long::sum);
        }
        return this.toCountItems(countMap);
    }

    private List<FaultAnalysisDistributionVO.CountItem> topFaultCodes(List<VehicleFault> faults) {
        Map<String, Long> countMap = faults.stream()
                .collect(Collectors.groupingBy(f -> f.getFaultCode() == null ? "UNKNOWN" : f.getFaultCode(), Collectors.counting()));
        return this.toCountItems(countMap).stream()
                .sorted(Comparator.comparing(FaultAnalysisDistributionVO.CountItem::getCount).reversed())
                .limit(10)
                .collect(Collectors.toList());
    }

    private List<FaultAnalysisDistributionVO.CountItem> toCountItems(Map<String, Long> countMap) {
        List<FaultAnalysisDistributionVO.CountItem> items = new ArrayList<>();
        countMap.forEach((name, count) -> {
            FaultAnalysisDistributionVO.CountItem item = new FaultAnalysisDistributionVO.CountItem();
            item.setName(name);
            item.setCount(count);
            items.add(item);
        });
        items.sort(Comparator.comparing(FaultAnalysisDistributionVO.CountItem::getCount).reversed());
        return items;
    }

    @Override
    public List<RiskVehicleVO> listRiskVehicles() {
        List<Vehicle> vehicles = this.vehicleMapper.selectList(
                new LambdaQueryWrapper<Vehicle>()
                        .eq(Vehicle::getDeleted, 0)
                        .and(w -> w.in(Vehicle::getRiskLevel, "MEDIUM", "HIGH")
                                .or().lt(Vehicle::getHealthScore, 75)
                                .or().isNull(Vehicle::getHealthScore))
                        .orderByAsc(Vehicle::getRiskLevel));
        List<RiskVehicleVO> result = new ArrayList<>();
        if (vehicles == null) {
            return result;
        }
        for (Vehicle vehicle : vehicles) {
            RiskVehicleVO vo = new RiskVehicleVO();
            vo.setId(vehicle.getId());
            vo.setVin(vehicle.getVin());
            vo.setPlateNumber(vehicle.getPlateNumber());
            vo.setModelId(vehicle.getModelId());
            vo.setHealthScore(vehicle.getHealthScore() == null ? 100 : vehicle.getHealthScore());
            String riskLevel = vehicle.getRiskLevel() == null ? "LOW" : vehicle.getRiskLevel();
            vo.setRiskLevel(riskLevel);
            long activeFaults = this.vehicleFaultMapper.selectCount(
                    new LambdaQueryWrapper<VehicleFault>()
                            .eq(VehicleFault::getVin, vehicle.getVin())
                            .eq(VehicleFault::getDeleted, 0)
                            .eq(VehicleFault::getStatus, 1));
            vo.setActiveFaultCount((int) activeFaults);
            StringBuilder reason = new StringBuilder();
            if ("HIGH".equals(riskLevel)) {
                reason.append("高风险等级");
            } else if ("MEDIUM".equals(riskLevel)) {
                reason.append("中风险等级");
            }
            if (activeFaults > 0) {
                if (reason.length() > 0) reason.append("，");
                reason.append(activeFaults).append(" 个未处理故障");
            }
            if (vo.getHealthScore() < 75) {
                if (reason.length() > 0) reason.append("，");
                reason.append("健康分偏低(").append(vo.getHealthScore()).append(")");
            }
            vo.setRiskReason(reason.length() == 0 ? "关注车辆" : reason.toString());
            vo.setLastOnlineTime(vehicle.getLastOnlineTime());
            if (vehicle.getModelId() != null) {
                VehicleModel model = this.vehicleModelMapper.selectById(vehicle.getModelId());
                if (model != null) {
                    vo.setModelName(model.getModelName());
                }
            }
            result.add(vo);
        }
        return result;
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

    private String formatTrendLabel(String mode, LocalDateTime statTime) {
        return switch (mode) {
            case "day" -> statTime.format(DAY_LABEL_FORMAT);
            case "week" -> statTime.format(WEEK_LABEL_FORMAT);
            case "month" -> statTime.format(MONTH_LABEL_FORMAT);
            default -> statTime.format(HOUR_LABEL_FORMAT);
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
        if (value instanceof Timestamp) {
            return ((Timestamp) value).toLocalDateTime();
        }
        if (value instanceof Date) {
            return LocalDateTime.ofInstant(((Date) value).toInstant(), ZoneId.systemDefault());
        }
        return LocalDateTime.parse(String.valueOf(value).replace(' ', 'T'));
    }
}
