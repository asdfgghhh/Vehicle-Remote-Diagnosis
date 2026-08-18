/*
 * Decompiled with CFR 0.152.
 */
package com.vrd.ecu.service;

import com.vrd.ecu.dto.EcuLogRecord;
import com.vrd.ecu.dto.PageResult;
import java.time.LocalDateTime;

public interface EcuLogClickHouseService {
    public PageResult<EcuLogRecord> search(Integer var1, Integer var2, String var3, String var4, LocalDateTime var5, LocalDateTime var6);

    public void insertRecord(EcuLogRecord var1);

    public EcuLogRecord getById(Long var1);

    public boolean existsByMd5(String var1);
}

