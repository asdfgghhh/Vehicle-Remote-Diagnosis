/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.baomidou.mybatisplus.core.conditions.Wrapper
 *  com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper
 *  com.baomidou.mybatisplus.core.metadata.IPage
 *  com.baomidou.mybatisplus.extension.plugins.pagination.Page
 *  com.vrd.common.result.Result
 *  org.springframework.web.bind.annotation.DeleteMapping
 *  org.springframework.web.bind.annotation.GetMapping
 *  org.springframework.web.bind.annotation.PathVariable
 *  org.springframework.web.bind.annotation.PostMapping
 *  org.springframework.web.bind.annotation.PutMapping
 *  org.springframework.web.bind.annotation.RequestBody
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RequestParam
 *  org.springframework.web.bind.annotation.RestController
 */
package com.vrd.vehicle.rule.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vrd.common.result.Result;
import com.vrd.vehicle.rule.engine.AlertRuleEngine;
import com.vrd.vehicle.rule.entity.AlertRule;
import com.vrd.vehicle.rule.entity.AlertTriggerLog;
import com.vrd.vehicle.rule.mapper.AlertRuleMapper;
import com.vrd.vehicle.rule.mapper.AlertTriggerLogMapper;
import java.time.LocalDateTime;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 告警规则与触发记录接口（服务端口 9082，网关路由前缀 /api/alert）
 * <p>
 * 管理告警规则引擎（Easy Rules）的规则 CRUD、启停与缓存刷新，
 * 并提供规则触发日志的分页查询。
 * 规则类型支持阈值（threshold）、趋势（trend）、组合（combination）三种。
 */
@RestController
@RequestMapping(value={"/alert"})
public class AlertRuleController {
    private final AlertRuleMapper alertRuleMapper;
    private final AlertTriggerLogMapper alertTriggerLogMapper;
    private final AlertRuleEngine alertRuleEngine;

    public AlertRuleController(AlertRuleMapper alertRuleMapper, AlertTriggerLogMapper alertTriggerLogMapper, AlertRuleEngine alertRuleEngine) {
        this.alertRuleMapper = alertRuleMapper;
        this.alertTriggerLogMapper = alertTriggerLogMapper;
        this.alertRuleEngine = alertRuleEngine;
    }

    /**
     * 分页查询告警规则列表
     * <p>GET /alert/rules
     *
     * @param page     当前页码，默认 1
     * @param size     每页条数，默认 20
     * @param keyword  可选，按规则名称模糊过滤
     * @param ruleType 可选，按规则类型过滤（threshold / trend / combination）
     * @return 分页结果 Page&lt;AlertRule&gt;（按优先级升序）
     */
    @GetMapping(value={"/rules"})
    public Result<Page<AlertRule>> listRules(@RequestParam(defaultValue="1") Integer page, @RequestParam(defaultValue="20") Integer size, @RequestParam(required=false) String keyword, @RequestParam(required=false) String ruleType) {
        Page<AlertRule> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<AlertRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AlertRule::getDeleted, 0);
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(AlertRule::getRuleName, keyword);
        }
        if (ruleType != null && !ruleType.isEmpty()) {
            wrapper.eq(AlertRule::getRuleType, ruleType);
        }
        wrapper.orderByAsc(AlertRule::getPriority);
        return Result.success(this.alertRuleMapper.selectPage(pageParam, wrapper));
    }

    /**
     * 新增告警规则
     * <p>POST /alert/rules
     * <p>创建成功后自动刷新规则引擎缓存，即时生效。
     *
     * @param rule 规则体（ruleName、ruleType、信号/阈值条件、级别、优先级、状态等）
     * @return 创建成功后的 AlertRule（含生成的 ID）
     */
    @PostMapping(value={"/rules"})
    public Result<AlertRule> createRule(@RequestBody AlertRule rule) {
        rule.setDeleted(0);
        rule.setCreateTime(LocalDateTime.now());
        rule.setUpdateTime(LocalDateTime.now());
        this.alertRuleMapper.insert(rule);
        this.alertRuleEngine.refreshRules();
        return Result.success(rule);
    }

    /**
     * 更新告警规则
     * <p>PUT /alert/rules/{id}
     * <p>更新后自动刷新规则引擎缓存，即时生效。
     *
     * @param id   规则 ID
     * @param rule 待更新的规则字段
     * @return 更新后的 AlertRule
     */
    @PutMapping(value={"/rules/{id}"})
    public Result<AlertRule> updateRule(@PathVariable Long id, @RequestBody AlertRule rule) {
        rule.setId(id);
        rule.setUpdateTime(LocalDateTime.now());
        this.alertRuleMapper.updateById(rule);
        this.alertRuleEngine.refreshRules();
        return Result.success(rule);
    }

    /**
     * 删除告警规则（逻辑删除，置 deleted=1）
     * <p>DELETE /alert/rules/{id}
     * <p>删除后自动刷新规则引擎缓存。
     *
     * @param id 规则 ID
     * @return 删除结果消息
     */
    @DeleteMapping(value={"/rules/{id}"})
    public Result<String> deleteRule(@PathVariable Long id) {
        AlertRule rule = (AlertRule)this.alertRuleMapper.selectById(id);
        if (rule != null) {
            rule.setDeleted(1);
            rule.setUpdateTime(LocalDateTime.now());
            this.alertRuleMapper.updateById(rule);
            this.alertRuleEngine.refreshRules();
        }
        return Result.success("\u5220\u9664\u6210\u529f");
    }

    /**
     * 启用/禁用告警规则
     * <p>PUT /alert/rules/{id}/status?status={0|1}
     * <p>切换后自动刷新规则引擎缓存。
     *
     * @param id     规则 ID
     * @param status 目标状态：1=启用，0=禁用
     * @return 操作结果消息（"已启用" / "已禁用"）
     */
    @PutMapping(value={"/rules/{id}/status"})
    public Result<String> toggleRule(@PathVariable Long id, @RequestParam Integer status) {
        AlertRule rule = (AlertRule)this.alertRuleMapper.selectById(id);
        if (rule != null) {
            rule.setStatus(status);
            rule.setUpdateTime(LocalDateTime.now());
            this.alertRuleMapper.updateById(rule);
            this.alertRuleEngine.refreshRules();
        }
        return Result.success(status == 1 ? "\u5df2\u542f\u7528" : "\u5df2\u7981\u7528");
    }

    /**
     * 手动刷新规则引擎缓存
     * <p>POST /alert/rules/refresh
     * <p>从数据库重新加载全部启用规则，用于规则缓存异常时的人工干预。
     *
     * @return 操作结果消息
     */
    @PostMapping(value={"/rules/refresh"})
    public Result<String> refreshRules() {
        this.alertRuleEngine.refreshRules();
        return Result.success("\u89c4\u5219\u7f13\u5b58\u5df2\u5237\u65b0");
    }

    /**
     * 分页查询规则触发日志
     * <p>GET /alert/logs
     *
     * @param page       当前页码，默认 1
     * @param size       每页条数，默认 20
     * @param vin        可选，按车架号过滤
     * @param alertLevel 可选，按告警级别过滤
     * @return 分页结果 Page&lt;AlertTriggerLog&gt;（按触发时间倒序）
     */
    @GetMapping(value={"/logs"})
    public Result<Page<AlertTriggerLog>> listTriggerLogs(@RequestParam(defaultValue="1") Integer page, @RequestParam(defaultValue="20") Integer size, @RequestParam(required=false) String vin, @RequestParam(required=false) Integer alertLevel) {
        Page<AlertTriggerLog> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<AlertTriggerLog> wrapper = new LambdaQueryWrapper<>();
        if (vin != null && !vin.isEmpty()) {
            wrapper.eq(AlertTriggerLog::getVin, vin);
        }
        if (alertLevel != null) {
            wrapper.eq(AlertTriggerLog::getAlertLevel, alertLevel);
        }
        wrapper.orderByDesc(AlertTriggerLog::getTriggerTime);
        return Result.success(this.alertTriggerLogMapper.selectPage(pageParam, wrapper));
    }
}

