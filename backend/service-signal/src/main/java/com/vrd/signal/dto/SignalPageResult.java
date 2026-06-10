package com.vrd.signal.dto;

import com.vrd.signal.entity.VehicleSignal;
import lombok.Data;

import java.util.List;

@Data
public class SignalPageResult {

    private List<VehicleSignal> records;
    private long total;
    private long current;
    private long size;

    public static SignalPageResult of(List<VehicleSignal> records, long total, long current, long size) {
        SignalPageResult result = new SignalPageResult();
        result.setRecords(records);
        result.setTotal(total);
        result.setCurrent(current);
        result.setSize(size);
        return result;
    }
}
