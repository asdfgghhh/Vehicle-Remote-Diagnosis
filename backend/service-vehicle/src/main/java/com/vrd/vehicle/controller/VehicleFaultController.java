package com.vrd.vehicle.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vrd.common.result.Result;
import com.vrd.vehicle.dto.FaultStandbyVO;
import com.vrd.vehicle.entity.FaultScene;
import com.vrd.vehicle.entity.VehicleFault;
import com.vrd.vehicle.service.VehicleFaultService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value={"/vehicle"})
public class VehicleFaultController {
    @Autowired
    private VehicleFaultService vehicleFaultService;

    @GetMapping(value={"/fault/page"})
    public Result<Page<VehicleFault>> faultPage(@RequestParam(value="current", defaultValue="1") Integer current,
                                                @RequestParam(value="size", defaultValue="10") Integer size,
                                                @RequestParam(value="vin", required=false) String vin,
                                                @RequestParam(value="faultCode", required=false) String faultCode,
                                                @RequestParam(value="level", required=false) String level,
                                                @RequestParam(value="status", required=false) Integer status,
                                                @RequestParam(value="sceneId", required=false) Long sceneId) {
        return Result.success(this.vehicleFaultService.pageFaults(current, size, vin, faultCode, level, status, sceneId));
    }

    @GetMapping(value={"/fault/standby"})
    public Result<FaultStandbyVO> faultStandby(@RequestParam(value="vin", required=false) String vin) {
        return Result.success(this.vehicleFaultService.getStandbyContext(vin));
    }

    @PostMapping(value={"/fault/{id}/diagnose"})
    public Result<Long> diagnoseFault(@PathVariable(value="id") Long id) {
        return Result.success(this.vehicleFaultService.diagnoseFault(id));
    }

    @GetMapping(value={"/fault-scene/list"})
    public Result<List<FaultScene>> sceneList() {
        return Result.success(this.vehicleFaultService.listScenes());
    }
}
