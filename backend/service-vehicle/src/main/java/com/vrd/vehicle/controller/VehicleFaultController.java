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

/**
 * 车辆故障接口（服务端口 9082，网关路由前缀 /api/vehicle/fault*）
 * <p>
 * 提供故障记录分页查询、待处理故障上下文、故障触发诊断、故障场景列表等能力。
 */
@RestController
@RequestMapping(value={"/vehicle"})
public class VehicleFaultController {
    @Autowired
    private VehicleFaultService vehicleFaultService;

    /**
     * 分页查询车辆故障记录
     * <p>GET /vehicle/fault/page
     *
     * @param current   当前页码，默认 1
     * @param size      每页条数，默认 10
     * @param vin       可选，按车架号过滤
     * @param faultCode 可选，按故障码过滤
     * @param level     可选，按故障级别过滤
     * @param status    可选，按处理状态过滤
     * @param sceneId   可选，按故障场景 ID 过滤
     * @return 分页结果 Page&lt;VehicleFault&gt;
     */
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

    /**
     * 查询待处理故障上下文（工作台模式）
     * <p>GET /vehicle/fault/standby
     *
     * @param vin 可选，按车架号过滤；为空时返回全部待处理上下文
     * @return FaultStandbyVO 待处理故障统计与上下文信息
     */
    @GetMapping(value={"/fault/standby"})
    public Result<FaultStandbyVO> faultStandby(@RequestParam(value="vin", required=false) String vin) {
        return Result.success(this.vehicleFaultService.getStandbyContext(vin));
    }

    /**
     * 对指定故障触发一次诊断
     * <p>POST /vehicle/fault/{id}/diagnose
     * <p>调用诊断服务对故障车辆执行远程诊断流程。
     *
     * @param id 故障记录 ID
     * @return Long 创建的诊断任务 ID
     */
    @PostMapping(value={"/fault/{id}/diagnose"})
    public Result<Long> diagnoseFault(@PathVariable(value="id") Long id) {
        return Result.success(this.vehicleFaultService.diagnoseFault(id));
    }

    /**
     * 查询全部故障场景（不分页）
     * <p>GET /vehicle/fault-scene/list
     * <p>用于故障筛选下拉与故障配置关联。
     *
     * @return List&lt;FaultScene&gt; 故障场景列表
     */
    @GetMapping(value={"/fault-scene/list"})
    public Result<List<FaultScene>> sceneList() {
        return Result.success(this.vehicleFaultService.listScenes());
    }
}
