package com.vrd.vehicle.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vrd.vehicle.dto.VehicleDomainHealthVO;
import com.vrd.vehicle.dto.VehicleHealthDetailVO;
import com.vrd.vehicle.dto.VehicleHealthTrendVO;
import com.vrd.vehicle.entity.Vehicle;
import com.vrd.vehicle.entity.VehicleFault;
import com.vrd.vehicle.entity.VehicleHealth;
import com.vrd.vehicle.entity.VehicleModel;
import com.vrd.vehicle.mapper.VehicleFaultMapper;
import com.vrd.vehicle.mapper.VehicleHealthMapper;
import com.vrd.vehicle.mapper.VehicleMapper;
import com.vrd.vehicle.mapper.VehicleModelMapper;
import com.vrd.vehicle.service.VehicleHealthService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VehicleHealthServiceImpl implements VehicleHealthService {
    private static final DateTimeFormatter DAY_FORMAT = DateTimeFormatter.ofPattern("MM-dd");

    @Autowired
    private VehicleHealthMapper vehicleHealthMapper;
    @Autowired
    private VehicleMapper vehicleMapper;
    @Autowired
    private VehicleModelMapper vehicleModelMapper;
    @Autowired
    private VehicleFaultMapper vehicleFaultMapper;

    @Override
    public VehicleHealthDetailVO getHealthDetail(String vin) {
        VehicleHealthDetailVO vo = new VehicleHealthDetailVO();
        vo.setVin(vin);
        Vehicle vehicle = this.vehicleMapper.selectOne(
                new LambdaQueryWrapper<Vehicle>().eq(Vehicle::getVin, vin).eq(Vehicle::getDeleted, 0));
        if (vehicle != null) {
            vo.setPlateNumber(vehicle.getPlateNumber());
            vo.setModelId(vehicle.getModelId());
            vo.setHealthScore(vehicle.getHealthScore());
            vo.setRiskLevel(vehicle.getRiskLevel());
            vo.setBatterySoh(vehicle.getBatterySoh());
            vo.setLastOnlineTime(vehicle.getLastOnlineTime());
            if (vehicle.getModelId() != null) {
                VehicleModel model = this.vehicleModelMapper.selectById(vehicle.getModelId());
                if (model != null) {
                    vo.setModelName(model.getModelName());
                }
            }
        }
        List<VehicleHealth> rows = this.vehicleHealthMapper.selectList(
                new LambdaQueryWrapper<VehicleHealth>().eq(VehicleHealth::getVin, vin));
        List<VehicleHealthDetailVO.DomainHealth> domains = new ArrayList<>();
        for (VehicleHealth row : rows) {
            VehicleHealthDetailVO.DomainHealth domain = new VehicleHealthDetailVO.DomainHealth();
            domain.setDomainCode(row.getDomainCode());
            domain.setDomainName(row.getDomainName());
            domain.setHealthScore(row.getHealthScore());
            domain.setStatus(row.getStatus());
            domain.setAlertCount(row.getAlertCount());
            domain.setRiskLevel(row.getRiskLevel());
            domain.setUpdateTime(row.getUpdateTime());
            domain.setComponents(this.parseComponents(row.getComponentJson()));
            domains.add(domain);
        }
        domains.sort(Comparator.comparing(VehicleHealthDetailVO.DomainHealth::getDomainCode));
        vo.setDomains(domains);
        if (vo.getHealthScore() == null && !domains.isEmpty()) {
            int total = 0;
            for (VehicleHealthDetailVO.DomainHealth d : domains) {
                total += d.getHealthScore() == null ? 100 : d.getHealthScore().intValue();
            }
            vo.setHealthScore(total / domains.size());
        }
        return vo;
    }

    @Override
    public VehicleHealthTrendVO getHealthTrend(String vin, Integer days) {
        int range = days != null && days > 0 ? days : 30;
        VehicleHealthTrendVO vo = new VehicleHealthTrendVO();
        vo.setVin(vin);
        vo.setGranularity("day");
        LocalDate start = LocalDate.now().minusDays(range - 1L);
        List<VehicleFault> faults = this.vehicleFaultMapper.selectList(
                new LambdaQueryWrapper<VehicleFault>()
                        .eq(VehicleFault::getVin, vin)
                        .eq(VehicleFault::getDeleted, 0)
                        .isNotNull(VehicleFault::getFaultTime)
                        .ge(VehicleFault::getFaultTime, start.atStartOfDay()));
        Map<LocalDate, Long> countByDay = faults.stream()
                .collect(Collectors.groupingBy(f -> f.getFaultTime().toLocalDate(), Collectors.counting()));
        List<VehicleHealthTrendVO.TrendPoint> points = new ArrayList<>();
        for (int i = 0; i < range; i++) {
            LocalDate day = start.plusDays(i);
            long faultCount = countByDay.getOrDefault(day, 0L);
            int score = Math.max(0, 100 - (int) (faultCount * 8L));
            VehicleHealthTrendVO.TrendPoint point = new VehicleHealthTrendVO.TrendPoint();
            point.setTimeLabel(day.format(DAY_FORMAT));
            point.setHealthScore(score);
            point.setStatus(this.resolveStatus(score));
            points.add(point);
        }
        vo.setPoints(points);
        return vo;
    }

    @Override
    public VehicleDomainHealthVO getDomainDetail(String vin, String domainCode) {
        VehicleDomainHealthVO vo = new VehicleDomainHealthVO();
        VehicleHealth row = this.vehicleHealthMapper.selectOne(
                new LambdaQueryWrapper<VehicleHealth>()
                        .eq(VehicleHealth::getVin, vin)
                        .eq(VehicleHealth::getDomainCode, domainCode));
        if (row == null) {
            return vo;
        }
        vo.setVin(row.getVin());
        vo.setDomainCode(row.getDomainCode());
        vo.setDomainName(row.getDomainName());
        vo.setHealthScore(row.getHealthScore());
        vo.setStatus(row.getStatus());
        vo.setAlertCount(row.getAlertCount());
        vo.setRiskLevel(row.getRiskLevel());
        vo.setUpdateTime(row.getUpdateTime());
        List<VehicleHealthDetailVO.ComponentHealth> components = this.parseComponents(row.getComponentJson());
        List<VehicleDomainHealthVO.ComponentHealth> domainComponents = new ArrayList<>();
        for (VehicleHealthDetailVO.ComponentHealth c : components) {
            VehicleDomainHealthVO.ComponentHealth dc = new VehicleDomainHealthVO.ComponentHealth();
            dc.setName(c.getName());
            dc.setScore(c.getScore());
            dc.setStatus(c.getStatus());
            domainComponents.add(dc);
        }
        vo.setComponents(domainComponents);
        return vo;
    }

    private List<VehicleHealthDetailVO.ComponentHealth> parseComponents(String componentJson) {
        List<VehicleHealthDetailVO.ComponentHealth> list = new ArrayList<>();
        if (componentJson == null || componentJson.isBlank()) {
            return list;
        }
        try {
            JSONArray arr = JSON.parseArray(componentJson);
            for (int i = 0; i < arr.size(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                VehicleHealthDetailVO.ComponentHealth c = new VehicleHealthDetailVO.ComponentHealth();
                c.setName(obj.getString("name"));
                c.setScore(obj.getInteger("score"));
                c.setStatus(obj.getString("status"));
                list.add(c);
            }
        } catch (Exception e) {
            // 解析失败时返回空列表，不影响主流程
        }
        return list;
    }

    private String resolveStatus(int score) {
        if (score >= 90) {
            return "NORMAL";
        }
        if (score >= 75) {
            return "ATTENTION";
        }
        if (score >= 60) {
            return "WARNING";
        }
        return "DANGER";
    }
}
