package com.vrd.signal.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.vrd.signal.entity.VehicleSignal;

import java.time.LocalDateTime;
import java.util.List;

public interface SignalService extends IService<VehicleSignal> {
    void receiveSignal(String vin, String payload);
    
    List<VehicleSignal> queryByTimeRange(Long vehicleId, LocalDateTime startTime, LocalDateTime endTime);
    
    Page<VehicleSignal> queryByTimeRangePaged(Long vehicleId, LocalDateTime startTime, LocalDateTime endTime, Integer current, Integer size);
    
    List<VehicleSignal> queryBySignalName(Long vehicleId, String signalName, LocalDateTime startTime, LocalDateTime endTime);
    
    void saveSignalBatch(List<VehicleSignal> signals);
}
