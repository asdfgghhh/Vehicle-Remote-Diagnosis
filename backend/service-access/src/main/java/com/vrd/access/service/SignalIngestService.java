/*
 * Decompiled with CFR 0.152.
 */
package com.vrd.access.service;

import com.vrd.access.entity.VehicleSignal;
import java.util.List;

public interface SignalIngestService {
    public void saveBatch(List<VehicleSignal> var1);
}

