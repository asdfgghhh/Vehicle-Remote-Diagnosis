/*
 * Decompiled with CFR 0.152.
 */
package com.vrd.ecu.service;

import com.vrd.ecu.dto.EcuLogRecord;
import com.vrd.ecu.dto.PageResult;
import java.io.File;
import java.time.LocalDateTime;

public interface EcuLogService {
    public PageResult<EcuLogRecord> page(Integer var1, Integer var2, String var3, String var4, LocalDateTime var5, LocalDateTime var6);

    public File downloadLog(Long var1);

    public EcuLogRecord getById(Long var1);
}

