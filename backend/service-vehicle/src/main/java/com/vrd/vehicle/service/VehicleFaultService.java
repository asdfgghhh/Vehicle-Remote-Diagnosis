package com.vrd.vehicle.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vrd.vehicle.dto.FaultStandbyVO;
import com.vrd.vehicle.entity.FaultScene;
import com.vrd.vehicle.entity.VehicleFault;
import java.util.List;

public interface VehicleFaultService {
    Page<VehicleFault> pageFaults(Integer current, Integer size, String vin, String faultCode, String level, Integer status, Long sceneId);

    FaultStandbyVO getStandbyContext(String vin);

    List<FaultScene> listScenes();

    Long diagnoseFault(Long faultId);
}
