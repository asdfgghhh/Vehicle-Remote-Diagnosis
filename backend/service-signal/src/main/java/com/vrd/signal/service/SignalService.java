/*
 * Decompiled with CFR 0.152.
 */
package com.vrd.signal.service;

import com.vrd.signal.dto.SignalPageResult;
import com.vrd.signal.entity.VehicleSignal;
import java.time.LocalDateTime;
import java.util.List;

public interface SignalService {
    public List<VehicleSignal> queryByTimeRange(String var1, Long var2, LocalDateTime var3, LocalDateTime var4);

    public SignalPageResult queryByTimeRangePaged(String var1, Long var2, LocalDateTime var3, LocalDateTime var4, Integer var5, Integer var6);

    public List<VehicleSignal> queryBySignalName(String var1, Long var2, String var3, LocalDateTime var4, LocalDateTime var5);

    public VehicleSignal getById(Long var1);
}

