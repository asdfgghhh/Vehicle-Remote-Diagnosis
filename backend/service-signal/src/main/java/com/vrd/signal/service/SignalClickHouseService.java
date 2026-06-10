package com.vrd.signal.service;

import com.vrd.signal.dto.SignalPageResult;
import com.vrd.signal.entity.VehicleSignal;

import java.time.LocalDateTime;
import java.util.List;

public interface SignalClickHouseService {

    List<VehicleSignal> queryByTimeRange(String vin, Long vehicleId, LocalDateTime startTime, LocalDateTime endTime);

    SignalPageResult queryByTimeRangePaged(String vin, Long vehicleId, LocalDateTime startTime, LocalDateTime endTime,
                                           int current, int size);

    List<VehicleSignal> queryBySignalName(String vin, Long vehicleId, String signalName,
                                          LocalDateTime startTime, LocalDateTime endTime);

    VehicleSignal getById(Long id);
}
