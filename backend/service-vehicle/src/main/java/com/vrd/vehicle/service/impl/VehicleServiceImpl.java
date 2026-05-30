package com.vrd.vehicle.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vrd.common.exception.BusinessException;
import com.vrd.vehicle.entity.SyncLog;
import com.vrd.vehicle.entity.Vehicle;
import com.vrd.vehicle.entity.VehicleEcu;
import com.vrd.vehicle.mapper.SyncLogMapper;
import com.vrd.vehicle.mapper.VehicleEcuMapper;
import com.vrd.vehicle.mapper.VehicleMapper;
import com.vrd.vehicle.service.VehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class VehicleServiceImpl extends ServiceImpl<VehicleMapper, Vehicle> implements VehicleService {

    @Autowired
    private VehicleEcuMapper vehicleEcuMapper;

    @Autowired
    private SyncLogMapper syncLogMapper;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private static final String VEHICLE_TOPIC = "vehicle-data";

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

    public void addEcu(VehicleEcu ecu) {
        ecu.setDeleted(0);
        ecu.setCreateTime(LocalDateTime.now());
        ecu.setUpdateTime(LocalDateTime.now());
        vehicleEcuMapper.insert(ecu);
    }

    public void updateEcu(VehicleEcu ecu) {
        ecu.setUpdateTime(LocalDateTime.now());
        vehicleEcuMapper.updateById(ecu);
    }

    public List<VehicleEcu> getEcusByVehicleId(Long vehicleId) {
        return vehicleEcuMapper.selectList(
                new LambdaQueryWrapper<VehicleEcu>()
                        .eq(VehicleEcu::getVehicleId, vehicleId)
                        .eq(VehicleEcu::getDeleted, 0)
        );
    }
}
