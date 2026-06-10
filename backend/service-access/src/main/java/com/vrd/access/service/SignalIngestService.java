package com.vrd.access.service;

import com.vrd.access.entity.VehicleSignal;

import java.util.List;

public interface SignalIngestService {

    void saveBatch(List<VehicleSignal> signals);
}
