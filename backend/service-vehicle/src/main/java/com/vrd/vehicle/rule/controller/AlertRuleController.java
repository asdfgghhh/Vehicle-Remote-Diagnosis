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

    @PostMapping(value={"/rules"})
    public Result<AlertRule> createRule(@RequestBody AlertRule rule) {
        rule.setDeleted(0);
        rule.setCreateTime(LocalDateTime.now());
        rule.setUpdateTime(LocalDateTime.now());
        this.alertRuleMapper.insert(rule);
        this.alertRuleEngine.refreshRules();
        return Result.success(rule);
    }

    @PutMapping(value={"/rules/{id}"})
    public Result<AlertRule> updateRule(@PathVariable Long id, @RequestBody AlertRule rule) {
        rule.setId(id);
        rule.setUpdateTime(LocalDateTime.now());
        this.alertRuleMapper.updateById(rule);
        this.alertRuleEngine.refreshRules();
        return Result.success(rule);
    }

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

    @PostMapping(value={"/rules/refresh"})
    public Result<String> refreshRules() {
        this.alertRuleEngine.refreshRules();
        return Result.success("\u89c4\u5219\u7f13\u5b58\u5df2\u5237\u65b0");
    }

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

