package com.vrd.signal.service.impl;

import com.vrd.signal.dto.SignalPageResult;
import com.vrd.signal.entity.VehicleSignal;
import com.vrd.signal.service.SignalClickHouseService;
import com.vrd.signal.service.SignalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SignalServiceImpl implements SignalService {

    @Autowired
    private SignalClickHouseService signalClickHouseService;

    @Override
    public List<VehicleSignal> queryByTimeRange(String vin, Long vehicleId, LocalDateTime startTime, LocalDateTime endTime) {
        return signalClickHouseService.queryByTimeRange(vin, vehicleId, startTime, endTime);
    }

    @Override
    public SignalPageResult queryByTimeRangePaged(String vin, Long vehicleId, LocalDateTime startTime, LocalDateTime endTime,
                                                  Integer current, Integer size) {
        return signalClickHouseService.queryByTimeRangePaged(
                vin, vehicleId, startTime, endTime, current == null ? 1 : current, size == null ? 50 : size);
    }

    @Override
    public List<VehicleSignal> queryBySignalName(String vin, Long vehicleId, String signalName,
                                                 LocalDateTime startTime, LocalDateTime endTime) {
        return signalClickHouseService.queryBySignalName(vin, vehicleId, signalName, startTime, endTime);
    }

    @Override
    public VehicleSignal getById(Long id) {
        return signalClickHouseService.getById(id);
    }
}
