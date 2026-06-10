package com.vrd.ecu.service;

import com.vrd.ecu.dto.EcuLogRecord;
import com.vrd.ecu.dto.PageResult;

import java.time.LocalDateTime;

public interface EcuLogClickHouseService {

    PageResult<EcuLogRecord> search(Integer current, Integer size, String vin, String ecuType,
                              LocalDateTime startTime, LocalDateTime endTime);

    void insertRecord(EcuLogRecord record);

    EcuLogRecord getById(Long id);

    boolean existsByMd5(String fileMd5);
}
