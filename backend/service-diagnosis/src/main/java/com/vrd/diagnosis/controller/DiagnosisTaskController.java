package com.vrd.diagnosis.controller;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vrd.common.result.Result;
import com.vrd.diagnosis.entity.UdsDiagnosisSession;
import com.vrd.diagnosis.mapper.UdsDiagnosisSessionMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value={"/diagnosis"})
public class DiagnosisTaskController {
    private static final Logger log = LoggerFactory.getLogger(DiagnosisTaskController.class);
    @Autowired
    private UdsDiagnosisSessionMapper sessionMapper;

    @PostMapping(value={"/task"})
    public Result<Map<String, Object>> createTask(@RequestBody Map<String, Object> body) {
        String vin = body.get("vin") == null ? null : String.valueOf(body.get("vin"));
        Object sceneIdObj = body.get("sceneId");
        Long sceneId = sceneIdObj == null ? null : Long.valueOf(String.valueOf(sceneIdObj));
        String diagLevel = body.get("diagLevel") == null ? "VEHICLE" : String.valueOf(body.get("diagLevel"));
        UdsDiagnosisSession session = new UdsDiagnosisSession();
        session.setTraceId(UUID.randomUUID().toString().replace("-", "").substring(0, 16));
        session.setVin(vin);
        session.setDiagLevel(diagLevel);
        session.setSceneId(sceneId);
        session.setSessionStatus("PENDING");
        session.setServiceId(0);
        session.setSuccess(0);
        session.setRemark("fault-tree task");
        session.setRequestTime(LocalDateTime.now());
        session.setCreateTime(LocalDateTime.now());
        this.sessionMapper.insert(session);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("taskId", session.getId());
        result.put("traceId", session.getTraceId());
        result.put("vin", vin);
        result.put("diagLevel", diagLevel);
        result.put("status", "PENDING");
        return Result.success(result);
    }

    @GetMapping(value={"/task/{id}"})
    public Result<Map<String, Object>> getTask(@PathVariable(value="id") Long id) {
        UdsDiagnosisSession session = this.sessionMapper.selectById(id);
        if (session == null) {
            return Result.success(null);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", session.getId());
        result.put("traceId", session.getTraceId());
        result.put("vin", session.getVin());
        result.put("vehicleId", session.getVehicleId());
        result.put("ecuType", session.getEcuType());
        result.put("diagLevel", session.getDiagLevel());
        result.put("sceneId", session.getSceneId());
        result.put("sessionStatus", session.getSessionStatus());
        result.put("success", session.getSuccess());
        result.put("requestData", session.getRequestData());
        result.put("responseData", session.getResponseData());
        result.put("requestTime", session.getRequestTime());
        result.put("responseTime", session.getResponseTime());
        result.put("createTime", session.getCreateTime());
        if (session.getFaultTreeResult() != null && !session.getFaultTreeResult().isBlank()) {
            result.put("faultTreeResult", JSON.parse(session.getFaultTreeResult()));
        } else {
            result.put("faultTreeResult", null);
        }
        return Result.success(result);
    }

    @GetMapping(value={"/report/{id}"})
    public Result<Map<String, Object>> getReport(@PathVariable(value="id") Long id) {
        UdsDiagnosisSession session = this.sessionMapper.selectById(id);
        Map<String, Object> report = new LinkedHashMap<>();
        if (session == null) {
            report.put("taskId", id);
            report.put("exists", false);
            return Result.success(report);
        }
        report.put("taskId", session.getId());
        report.put("vin", session.getVin());
        report.put("diagLevel", session.getDiagLevel());
        report.put("sceneId", session.getSceneId());
        report.put("status", session.getSessionStatus());
        report.put("success", session.getSuccess());
        report.put("startTime", session.getRequestTime());
        report.put("endTime", session.getResponseTime());
        String levelName = "VEHICLE".equals(session.getDiagLevel()) ? "整车级诊断"
                : "DOMAIN".equals(session.getDiagLevel()) ? "域级诊断" : "部件级诊断";
        report.put("levelName", levelName);
        if (session.getFaultTreeResult() != null && !session.getFaultTreeResult().isBlank()) {
            report.put("faultTree", JSON.parse(session.getFaultTreeResult()));
        }
        List<Map<String, Object>> summary = new ArrayList<>();
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("key", "诊断结论");
        item.put("value", session.getSuccess() != null && session.getSuccess() == 1 ? "正常" : "存在异常，建议联系售后或远程升级");
        summary.add(item);
        if (session.getRemark() != null && !session.getRemark().isBlank()) {
            Map<String, Object> remark = new LinkedHashMap<>();
            remark.put("key", "备注");
            remark.put("value", session.getRemark());
            summary.add(remark);
        }
        report.put("summary", summary);
        return Result.success(report);
    }

    @GetMapping(value={"/level"})
    public Result<Map<String, Object>> getLevels() {
        List<Map<String, Object>> levels = new ArrayList<>();
        Map<String, Object> vehicleLevel = new LinkedHashMap<>();
        vehicleLevel.put("code", "VEHICLE");
        vehicleLevel.put("name", "整车级诊断");
        vehicleLevel.put("description", "全车故障树扫描，覆盖 320+ 故障树节点，快速定位问题域");
        vehicleLevel.put("supportedServices", List.of("0x19", "0x22", "0x27", "0x10"));
        levels.add(vehicleLevel);
        Map<String, Object> domainLevel = new LinkedHashMap<>();
        domainLevel.put("code", "DOMAIN");
        domainLevel.put("name", "域级诊断");
        domainLevel.put("description", "针对指定功能域（智驾/座舱/动力/底盘/车身/三电/网联）深度扫描");
        domainLevel.put("supportedServices", List.of("0x19", "0x22", "0x2E", "0x31", "0x27"));
        levels.add(domainLevel);
        Map<String, Object> partLevel = new LinkedHashMap<>();
        partLevel.put("code", "PART");
        partLevel.put("name", "部件级诊断");
        partLevel.put("description", "精确到 ECU 部件，执行读写数据、例程控制、IO 控制等专项诊断");
        partLevel.put("supportedServices", List.of("0x19", "0x22", "0x2E", "0x31", "0x2F", "0x23", "0x3D", "0x27"));
        levels.add(partLevel);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("levels", levels);
        return Result.success(result);
    }

    @GetMapping(value={"/fault-tree/scenarios"})
    public Result<List<Map<String, Object>>> getFaultTreeScenarios() {
        List<Map<String, Object>> scenes = new ArrayList<>();
        scenes.add(this.scene("SCENE_ALL", "全车通用扫描", "全车 320+ 故障树扫描", 0, 0.90));
        scenes.add(this.scene("SCENE_PARK", "车辆趴窝", "动力中断无法行驶", 1, 0.93));
        scenes.add(this.scene("SCENE_THERMAL", "电池热失控", "电池温度异常/热失控风险", 1, 0.96));
        scenes.add(this.scene("SCENE_BRAKE", "制动力不足", "制动系统效能下降", 1, 0.91));
        scenes.add(this.scene("SCENE_COLLISION", "碰撞", "碰撞事件检测", 1, 0.88));
        scenes.add(this.scene("SCENE_OTA_FAIL", "OTA 升级失败", "OTA 升级失败/回滚", 1, 0.90));
        scenes.add(this.scene("SCENE_COMM_LOST", "通讯异常", "ECU 通讯丢失", 1, 0.92));
        return Result.success(scenes);
    }

    private Map<String, Object> scene(String code, String name, String desc, int priority, double confidence) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("sceneCode", code);
        map.put("sceneName", name);
        map.put("description", desc);
        map.put("priority", priority);
        map.put("aiConfidence", confidence);
        return map;
    }

    @GetMapping(value={"/task/page"})
    public Result<Object> pageTasks(@RequestParam(value="vin", required=false) String vin,
                                    @RequestParam(value="current", defaultValue="1") Integer current,
                                    @RequestParam(value="size", defaultValue="10") Integer size) {
        LambdaQueryWrapper<UdsDiagnosisSession> wrapper = new LambdaQueryWrapper<>();
        if (vin != null && !vin.isBlank()) {
            wrapper.eq(UdsDiagnosisSession::getVin, vin);
        }
        wrapper.orderByDesc(UdsDiagnosisSession::getCreateTime);
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<UdsDiagnosisSession> page =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(current, size);
        return Result.success(this.sessionMapper.selectPage(page, wrapper));
    }
}
