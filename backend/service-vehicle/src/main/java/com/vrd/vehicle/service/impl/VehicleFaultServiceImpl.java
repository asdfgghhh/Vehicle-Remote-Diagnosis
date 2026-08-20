package com.vrd.vehicle.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vrd.vehicle.dto.FaultStandbyVO;
import com.vrd.vehicle.entity.FaultConfig;
import com.vrd.vehicle.entity.FaultScene;
import com.vrd.vehicle.entity.MaintenanceRecord;
import com.vrd.vehicle.entity.Vehicle;
import com.vrd.vehicle.entity.VehicleFault;
import com.vrd.vehicle.entity.VehicleModel;
import com.vrd.vehicle.mapper.FaultConfigMapper;
import com.vrd.vehicle.mapper.FaultSceneMapper;
import com.vrd.vehicle.mapper.MaintenanceRecordMapper;
import com.vrd.vehicle.mapper.VehicleFaultMapper;
import com.vrd.vehicle.mapper.VehicleMapper;
import com.vrd.vehicle.mapper.VehicleModelMapper;
import com.vrd.vehicle.service.VehicleFaultService;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class VehicleFaultServiceImpl implements VehicleFaultService {
    private static final Logger log = LoggerFactory.getLogger(VehicleFaultServiceImpl.class);
    @Autowired
    private VehicleFaultMapper vehicleFaultMapper;
    @Autowired
    private FaultSceneMapper faultSceneMapper;
    @Autowired
    private FaultConfigMapper faultConfigMapper;
    @Autowired
    private MaintenanceRecordMapper maintenanceRecordMapper;
    @Autowired
    private VehicleMapper vehicleMapper;
    @Autowired
    private VehicleModelMapper vehicleModelMapper;
    @Value("${vrd.diagnosis-service-url:http://service-diagnosis:9087}")
    private String diagnosisServiceUrl;

    @Override
    public Page<VehicleFault> pageFaults(Integer current, Integer size, String vin, String faultCode, String level, Integer status, Long sceneId) {
        LambdaQueryWrapper<VehicleFault> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VehicleFault::getDeleted, 0);
        if (vin != null && !vin.isBlank()) {
            wrapper.like(VehicleFault::getVin, vin);
        }
        if (faultCode != null && !faultCode.isBlank()) {
            wrapper.like(VehicleFault::getFaultCode, faultCode);
        }
        if (status != null) {
            wrapper.eq(VehicleFault::getStatus, status);
        }
        if (level != null && !level.isBlank()) {
            List<String> codes = this.resolveFaultCodesByLevel(level);
            if (codes.isEmpty()) {
                wrapper.eq(VehicleFault::getId, -1L);
            } else {
                wrapper.in(VehicleFault::getFaultCode, codes);
            }
        }
        if (sceneId != null) {
            FaultScene scene = this.faultSceneMapper.selectById(sceneId);
            if (scene != null && scene.getFaultCodes() != null && !scene.getFaultCodes().isBlank()) {
                List<String> codes = new ArrayList<>();
                for (String c : scene.getFaultCodes().split(",")) {
                    if (!c.isBlank()) {
                        codes.add(c.trim());
                    }
                }
                if (!codes.isEmpty()) {
                    wrapper.in(VehicleFault::getFaultCode, codes);
                }
            }
        }
        wrapper.orderByDesc(VehicleFault::getFaultTime);
        return this.vehicleFaultMapper.selectPage(new Page<>(current, size), wrapper);
    }

    private List<String> resolveFaultCodesByLevel(String level) {
        Integer alarmLevel;
        try {
            alarmLevel = Integer.valueOf(level);
        } catch (NumberFormatException e) {
            return List.of();
        }
        List<FaultConfig> configs = this.faultConfigMapper.selectList(
                new LambdaQueryWrapper<FaultConfig>()
                        .eq(FaultConfig::getAlarmLevel, alarmLevel)
                        .eq(FaultConfig::getDeleted, 0));
        List<String> codes = new ArrayList<>();
        for (FaultConfig config : configs) {
            if (config.getFaultCode() != null && !codes.contains(config.getFaultCode())) {
                codes.add(config.getFaultCode());
            }
        }
        return codes;
    }

    @Override
    public FaultStandbyVO getStandbyContext(String vin) {
        FaultStandbyVO vo = new FaultStandbyVO();
        vo.setVin(vin);
        Vehicle vehicle = this.vehicleMapper.selectOne(
                new LambdaQueryWrapper<Vehicle>().eq(Vehicle::getVin, vin).eq(Vehicle::getDeleted, 0));
        if (vehicle != null) {
            vo.setPlateNumber(vehicle.getPlateNumber());
            vo.setHealthScore(vehicle.getHealthScore());
            vo.setRiskLevel(vehicle.getRiskLevel());
            if (vehicle.getModelId() != null) {
                VehicleModel model = this.vehicleModelMapper.selectById(vehicle.getModelId());
                if (model != null) {
                    vo.setModelName(model.getModelName());
                }
            }
        }
        List<VehicleFault> pendingFaults = this.vehicleFaultMapper.selectList(
                new LambdaQueryWrapper<VehicleFault>()
                        .eq(VehicleFault::getVin, vin)
                        .eq(VehicleFault::getDeleted, 0)
                        .eq(VehicleFault::getStatus, 1)
                        .orderByDesc(VehicleFault::getFaultTime)
                        .last("LIMIT 20"));
        vo.setPendingFaultCount(pendingFaults.size());
        List<FaultStandbyVO.FaultItem> faultItems = new ArrayList<>();
        for (VehicleFault fault : pendingFaults) {
            FaultStandbyVO.FaultItem item = new FaultStandbyVO.FaultItem();
            item.setId(fault.getId());
            item.setFaultCode(fault.getFaultCode());
            item.setFaultName(fault.getFaultName());
            item.setComponentCode(fault.getComponentCode());
            item.setStatus(fault.getStatus());
            item.setFaultTime(fault.getFaultTime());
            faultItems.add(item);
        }
        vo.setPendingFaults(faultItems);
        LocalDate since = LocalDate.now().minusDays(90L);
        List<MaintenanceRecord> records = this.maintenanceRecordMapper.selectList(
                new LambdaQueryWrapper<MaintenanceRecord>()
                        .eq(MaintenanceRecord::getVin, vin)
                        .ge(MaintenanceRecord::getRecordDate, since)
                        .orderByDesc(MaintenanceRecord::getRecordDate)
                        .last("LIMIT 10"));
        List<FaultStandbyVO.MaintenanceItem> maintenanceItems = new ArrayList<>();
        for (MaintenanceRecord record : records) {
            FaultStandbyVO.MaintenanceItem item = new FaultStandbyVO.MaintenanceItem();
            item.setId(record.getId());
            item.setTitle(record.getTitle());
            item.setRecordType(record.getRecordType());
            item.setRecordDate(record.getRecordDate());
            item.setMileage(record.getMileage());
            item.setOperator(record.getOperator());
            item.setContent(record.getContent());
            maintenanceItems.add(item);
        }
        vo.setMaintenanceRecords(maintenanceItems);
        vo.setAiPriority(this.buildAiPriority(vin, vehicle, pendingFaults));
        return vo;
    }

    private List<FaultStandbyVO.PriorityItem> buildAiPriority(String vin, Vehicle vehicle, List<VehicleFault> pendingFaults) {
        List<FaultStandbyVO.PriorityItem> items = new ArrayList<>();
        if (vehicle != null && "HIGH".equals(vehicle.getRiskLevel())) {
            items.add(this.priorityItem(1, "高风险车辆", "整车风险等级为 HIGH，健康分 " + vehicle.getHealthScore(), "立即安排整车级故障树扫描"));
        }
        List<FaultScene> scenes = this.faultSceneMapper.selectList(
                new LambdaQueryWrapper<FaultScene>().eq(FaultScene::getStatus, 1).orderByAsc(FaultScene::getPriority));
        Map<String, FaultScene> sceneByCodes = new HashMap<>();
        for (FaultScene scene : scenes) {
            if (scene.getFaultCodes() == null) {
                continue;
            }
            for (String c : scene.getFaultCodes().split(",")) {
                sceneByCodes.putIfAbsent(c.trim(), scene);
            }
        }
        int rank = 2;
        for (VehicleFault fault : pendingFaults) {
            FaultScene scene = sceneByCodes.get(fault.getFaultCode());
            if (scene == null) {
                continue;
            }
            items.add(this.priorityItem(rank, scene.getSceneName(), "故障 " + fault.getFaultCode() + " 命中场景 " + scene.getSceneCode(), "按场景执行诊断序列"));
            rank++;
        }
        return items;
    }

    private FaultStandbyVO.PriorityItem priorityItem(int priority, String sceneName, String reason, String action) {
        FaultStandbyVO.PriorityItem item = new FaultStandbyVO.PriorityItem();
        item.setPriority(priority);
        item.setSceneName(sceneName);
        item.setReason(reason);
        item.setAction(action);
        return item;
    }

    @Override
    public List<FaultScene> listScenes() {
        return this.faultSceneMapper.selectList(
                new LambdaQueryWrapper<FaultScene>().eq(FaultScene::getStatus, 1).eq(FaultScene::getDeleted, 0));
    }

    @Override
    public Long diagnoseFault(Long faultId) {
        VehicleFault fault = this.vehicleFaultMapper.selectById(faultId);
        if (fault == null) {
            throw new com.vrd.common.exception.BusinessException("故障记录不存在");
        }
        Long sceneId = null;
        List<FaultScene> scenes = this.faultSceneMapper.selectList(
                new LambdaQueryWrapper<FaultScene>().eq(FaultScene::getStatus, 1).eq(FaultScene::getDeleted, 0));
        for (FaultScene scene : scenes) {
            if (scene.getFaultCodes() != null && scene.getFaultCodes().contains(fault.getFaultCode())) {
                sceneId = scene.getId();
                break;
            }
        }
        if (sceneId == null) {
            for (FaultScene scene : scenes) {
                if ("SCENE_ALL".equals(scene.getSceneCode())) {
                    sceneId = scene.getId();
                    break;
                }
            }
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("vin", fault.getVin());
        body.put("sceneId", sceneId);
        body.put("diagLevel", "VEHICLE");
        try {
            String baseUrl = this.diagnosisServiceUrl.endsWith("/") ? this.diagnosisServiceUrl : this.diagnosisServiceUrl + "/";
            String response = WebClient.create(baseUrl + "diagnosis/task").post()
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            if (response != null) {
                Map<String, Object> parsed = JSON.parseObject(response);
                Object data = parsed.get("data");
                if (data instanceof Map) {
                    Map<?, ?> dataMap = (Map<?, ?>) data;
                    Object taskId = dataMap.get("taskId");
                    if (taskId != null) {
                        return Long.valueOf(String.valueOf(taskId));
                    }
                }
            }
        } catch (Exception e) {
            log.warn("调用诊断服务创建任务失败: {}", e.getMessage());
        }
        throw new com.vrd.common.exception.BusinessException("远程诊断任务创建失败，请确认诊断服务可用");
    }
}
