package com.vrd.signal.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vrd.signal.entity.SignalBatch;
import com.vrd.signal.entity.VehicleSignal;
import com.vrd.signal.mapper.SignalBatchMapper;
import com.vrd.signal.mapper.VehicleSignalMapper;
import com.vrd.signal.service.SignalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class SignalServiceImpl extends ServiceImpl<VehicleSignalMapper, VehicleSignal> implements SignalService {

    @Autowired
    private SignalBatchMapper signalBatchMapper;

    @Override
    public void receiveSignal(String vin, String payload) {
        try {
            JSONObject data = JSON.parseObject(payload);
            
            SignalBatch batch = new SignalBatch();
            batch.setVin(vin);
            batch.setRawData(payload);
            batch.setStatus(1);
            batch.setDeleted(0);
            batch.setCreateTime(LocalDateTime.now());
            batch.setUpdateTime(LocalDateTime.now());
            
            signalBatchMapper.insert(batch);
            
            List<VehicleSignal> signals = new ArrayList<>();
            
            if (data.containsKey("signals")) {
                JSONArray signalArray = data.getJSONArray("signals");
                
                for (int i = 0; i < signalArray.size(); i++) {
                    JSONObject signalObj = signalArray.getJSONObject(i);
                    
                    VehicleSignal signal = new VehicleSignal();
                    signal.setVin(vin);
                    signal.setSignalName(signalObj.getString("name"));
                    signal.setSignalValue(signalObj.getString("value"));
                    
                    String valueStr = signalObj.getString("value");
                    try {
                        signal.setNumericValue(new BigDecimal(valueStr));
                    } catch (NumberFormatException e) {
                        signal.setNumericValue(BigDecimal.ZERO);
                    }
                    
                    signal.setUnit(signalObj.getString("unit"));
                    signal.setTimestamp(signalObj.getLong("timestamp"));
                    signal.setMessageName(signalObj.getString("messageName"));
                    signal.setMessageId(signalObj.getInteger("messageId"));
                    signal.setDeleted(0);
                    signal.setCreateTime(LocalDateTime.now());
                    
                    if (signal.getTimestamp() != null) {
                        long ts = signal.getTimestamp();
                        signal.setSignalTime(LocalDateTime.ofEpochSecond(
                                ts / 1000, 0, java.time.ZoneOffset.ofHours(8)));
                    } else {
                        signal.setSignalTime(LocalDateTime.now());
                    }
                    
                    signals.add(signal);
                }
            }
            
            if (!signals.isEmpty()) {
                saveBatch(signals);
                batch.setSignalCount(signals.size());
                batch.setParsedData(JSON.toJSONString(signals));
                batch.setStatus(2);
                signalBatchMapper.updateById(batch);
            }
            
        } catch (Exception e) {
            SignalBatch batch = new SignalBatch();
            batch.setVin(vin);
            batch.setRawData(payload);
            batch.setStatus(3);
            batch.setDeleted(0);
            batch.setCreateTime(LocalDateTime.now());
            signalBatchMapper.insert(batch);
        }
    }

    @Override
    public List<VehicleSignal> queryByTimeRange(Long vehicleId, LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<VehicleSignal> wrapper = new LambdaQueryWrapper<>();
        
        if (vehicleId != null) {
            wrapper.eq(VehicleSignal::getVehicleId, vehicleId);
        }
        
        wrapper.between(VehicleSignal::getSignalTime, startTime, endTime)
                .eq(VehicleSignal::getDeleted, 0)
                .orderByAsc(VehicleSignal::getSignalTime);
        
        return list(wrapper);
    }

    @Override
    public Page<VehicleSignal> queryByTimeRangePaged(Long vehicleId, LocalDateTime startTime, LocalDateTime endTime, Integer current, Integer size) {
        Page<VehicleSignal> page = new Page<>(current, size);
        LambdaQueryWrapper<VehicleSignal> wrapper = new LambdaQueryWrapper<>();
        
        if (vehicleId != null) {
            wrapper.eq(VehicleSignal::getVehicleId, vehicleId);
        }
        
        wrapper.between(VehicleSignal::getSignalTime, startTime, endTime)
                .eq(VehicleSignal::getDeleted, 0)
                .orderByAsc(VehicleSignal::getSignalTime);
        
        IPage<VehicleSignal> result = page(page, wrapper);
        return (Page<VehicleSignal>) result;
    }

    @Override
    public List<VehicleSignal> queryBySignalName(Long vehicleId, String signalName, LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<VehicleSignal> wrapper = new LambdaQueryWrapper<>();
        
        if (vehicleId != null) {
            wrapper.eq(VehicleSignal::getVehicleId, vehicleId);
        }
        
        wrapper.eq(VehicleSignal::getSignalName, signalName)
                .between(VehicleSignal::getSignalTime, startTime, endTime)
                .eq(VehicleSignal::getDeleted, 0)
                .orderByAsc(VehicleSignal::getSignalTime);
        
        return list(wrapper);
    }

    @Override
    public void saveSignalBatch(List<VehicleSignal> signals) {
        saveBatch(signals);
    }
}
