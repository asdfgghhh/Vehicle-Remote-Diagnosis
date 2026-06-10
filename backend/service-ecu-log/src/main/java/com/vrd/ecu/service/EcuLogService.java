package com.vrd.ecu.service;

import com.vrd.ecu.dto.EcuLogRecord;
import com.vrd.ecu.dto.PageResult;

import java.io.File;
import java.time.LocalDateTime;

public interface EcuLogService {

    PageResult<EcuLogRecord> page(Integer current, Integer size, String vin, String ecuType,
                            LocalDateTime startTime, LocalDateTime endTime);

    File downloadLog(Long recordId);

    EcuLogRecord getById(Long recordId);
}
