package com.vrd.access.service;

import com.vrd.access.dto.EcuLogRecord;

public interface EcuLogIngestService {

    void insertRecord(EcuLogRecord record);

    boolean existsByMd5(String fileMd5);
}
