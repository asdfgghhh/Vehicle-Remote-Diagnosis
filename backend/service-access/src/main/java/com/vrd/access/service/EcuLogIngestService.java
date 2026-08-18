/*
 * Decompiled with CFR 0.152.
 */
package com.vrd.access.service;

import com.vrd.access.dto.EcuLogRecord;

public interface EcuLogIngestService {
    public void insertRecord(EcuLogRecord var1);

    public boolean existsByMd5(String var1);
}

