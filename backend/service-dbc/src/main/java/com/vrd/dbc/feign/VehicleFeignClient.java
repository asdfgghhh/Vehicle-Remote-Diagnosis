package com.vrd.dbc.feign;

import com.vrd.common.result.Result;
import com.vrd.dbc.dto.VehicleInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * service-vehicle 远程调用客户端。
 * <p>用于 service-dbc 调用 service-vehicle 的车辆档案接口，
 * 通过 Nacos 服务发现解析 service-vehicle 实例。
 * <p>注：service-vehicle 的 VehicleController 类级别注解为
 * {@code @RequestMapping("/vehicle")}，Feign 的 path 需与之保持一致。
 */
@FeignClient(name = "service-vehicle", path = "/vehicle")
public interface VehicleFeignClient {

    /**
     * 按 ID 查询车辆详情
     * <p>对应 GET /vehicle/{id}
     *
     * @param id 车辆 ID
     * @return Result&lt;VehicleInfo&gt; 车辆精简信息；不存在时 data 为 null
     */
    @GetMapping("/{id}")
    Result<VehicleInfo> getById(@PathVariable("id") Long id);

    /**
     * 按车型分页查询车辆 ID + VIN 列表
     * <p>对应 GET /vehicle/list-by-model?modelId={modelId}
     * <p>用于 DBC 按车型下发时批量获取目标车辆。
     * service-vehicle 返回 Map 列表，Feign 通过 Jackson 反序列化为 VehicleInfo。
     *
     * @param modelId 车型 ID
     * @param current 当前页码，从 1 开始
     * @param size    每页条数
     * @return Result&lt;List&lt;VehicleInfo&gt;&gt; 每项含 id 和 vin
     */
    @GetMapping("/list-by-model")
    Result<List<VehicleInfo>> listByModel(
            @RequestParam("modelId") Long modelId,
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam(value = "size", defaultValue = "500") Integer size);
}
