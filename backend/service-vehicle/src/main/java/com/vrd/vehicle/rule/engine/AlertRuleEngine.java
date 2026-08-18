/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.baomidou.mybatisplus.core.conditions.Wrapper
 *  com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper
 *  org.jeasy.rules.api.Facts
 *  org.jeasy.rules.api.Rule
 *  org.jeasy.rules.api.Rules
 *  org.jeasy.rules.api.RulesEngine
 *  org.jeasy.rules.api.RulesEngineParameters
 *  org.jeasy.rules.core.DefaultRulesEngine
 *  org.jeasy.rules.mvel.MVELRule
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.stereotype.Component
 */
package com.vrd.vehicle.rule.engine;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vrd.vehicle.entity.VehicleAlert;
import com.vrd.vehicle.mapper.VehicleAlertMapper;
import com.vrd.vehicle.rule.engine.SignalDataContext;
import com.vrd.vehicle.rule.entity.AlertRule;
import com.vrd.vehicle.rule.entity.AlertTriggerLog;
import com.vrd.vehicle.rule.mapper.AlertRuleMapper;
import com.vrd.vehicle.rule.mapper.AlertTriggerLogMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.jeasy.rules.api.Facts;
import org.jeasy.rules.api.Rule;
import org.jeasy.rules.api.Rules;
import org.jeasy.rules.api.RulesEngine;
import org.jeasy.rules.api.RulesEngineParameters;
import org.jeasy.rules.core.DefaultRulesEngine;
import org.jeasy.rules.mvel.MVELRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AlertRuleEngine {
    private static final Logger log = LoggerFactory.getLogger(AlertRuleEngine.class);
    private final AlertRuleMapper alertRuleMapper;
    private final AlertTriggerLogMapper alertTriggerLogMapper;
    private final VehicleAlertMapper vehicleAlertMapper;
    private final Map<Long, AlertRule> ruleCache = new ConcurrentHashMap<Long, AlertRule>();
    private final Map<String, Integer> hitCounter = new ConcurrentHashMap<String, Integer>();
    private final Map<String, Long> cooldownMap = new ConcurrentHashMap<String, Long>();
    private final Map<String, LinkedList<BigDecimal>> signalHistory = new ConcurrentHashMap<String, LinkedList<BigDecimal>>();
    private final RulesEngine rulesEngine;

    public AlertRuleEngine(AlertRuleMapper alertRuleMapper, AlertTriggerLogMapper alertTriggerLogMapper, VehicleAlertMapper vehicleAlertMapper) {
        this.alertRuleMapper = alertRuleMapper;
        this.alertTriggerLogMapper = alertTriggerLogMapper;
        this.vehicleAlertMapper = vehicleAlertMapper;
        RulesEngineParameters parameters = new RulesEngineParameters().skipOnFirstAppliedRule(false).skipOnFirstFailedRule(false).skipOnFirstNonTriggeredRule(false);
        DefaultRulesEngine engine = new DefaultRulesEngine(parameters);
        this.rulesEngine = engine;
        this.refreshRules();
    }

    public void refreshRules() {
        List rules = this.alertRuleMapper.selectList((Wrapper)((LambdaQueryWrapper)new LambdaQueryWrapper().eq(AlertRule::getStatus, (Object)1)).eq(AlertRule::getDeleted, (Object)0));
        this.ruleCache.clear();
        for (AlertRule rule : rules) {
            this.ruleCache.put(rule.getId(), rule);
        }
        log.info("Alert rules refreshed: {} rules loaded", (Object)rules.size());
    }

    public void evaluate(String vin, Long vehicleId, String signalName, BigDecimal value, String messageName) {
        if (vin == null || signalName == null || value == null) {
            return;
        }
        this.updateSignalHistory(vin, signalName, value);
        for (AlertRule rule : this.ruleCache.values()) {
            SignalDataContext context;
            boolean triggered;
            if (!signalName.equals(rule.getSignalName()) || this.isInCooldown(rule.getId(), vin) || !(triggered = this.evaluateRule(rule, context = this.buildContext(vin, vehicleId, signalName, value, messageName, rule)))) continue;
            this.handleAlert(rule, context);
        }
    }

    private boolean evaluateRule(AlertRule rule, SignalDataContext context) {
        try {
            switch (rule.getRuleType()) {
                case "THRESHOLD": {
                    return this.evaluateThreshold(rule, context);
                }
                case "TREND": {
                    return this.evaluateTrend(rule, context);
                }
                case "COMBINATION": {
                    return this.evaluateCombination(rule, context);
                }
            }
            return this.evaluateThreshold(rule, context);
        }
        catch (Exception e) {
            log.error("Rule evaluation error: ruleId={}, type={}", new Object[]{rule.getId(), rule.getRuleType(), e});
            return false;
        }
    }

    private boolean evaluateThreshold(AlertRule rule, SignalDataContext context) {
        boolean thresholdExceeded = false;
        if (rule.getUpperThreshold() != null && context.getCurrentValue() != null && context.getCurrentValue().compareTo(BigDecimal.valueOf(rule.getUpperThreshold())) > 0) {
            thresholdExceeded = true;
        }
        if (rule.getLowerThreshold() != null && context.getCurrentValue() != null && context.getCurrentValue().compareTo(BigDecimal.valueOf(rule.getLowerThreshold())) < 0) {
            thresholdExceeded = true;
        }
        if (!thresholdExceeded) {
            this.resetHitCounter(rule.getId(), context.getVin());
            return false;
        }
        int consecutive = rule.getConsecutiveCount() != null ? rule.getConsecutiveCount() : 1;
        int currentHits = this.incrementHitCounter(rule.getId(), context.getVin());
        return currentHits >= consecutive;
    }

    private boolean evaluateTrend(AlertRule rule, SignalDataContext context) {
        double rateThreshold;
        List<BigDecimal> history = this.getSignalHistory(context.getVin(), context.getSignalName());
        if (history.size() < 3) {
            return false;
        }
        BigDecimal first = history.get(0);
        BigDecimal last = history.get(history.size() - 1);
        if (first.compareTo(BigDecimal.ZERO) == 0) {
            return false;
        }
        BigDecimal changeRate = last.subtract(first).divide(first, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100L));
        context.setTrendChangeRate(changeRate);
        double d = rateThreshold = rule.getTrendChangeRate() != null ? rule.getTrendChangeRate() : 10.0;
        if ("UP".equals(rule.getTrendDirection())) {
            return changeRate.compareTo(BigDecimal.valueOf(rateThreshold)) > 0;
        }
        if ("DOWN".equals(rule.getTrendDirection())) {
            return changeRate.compareTo(BigDecimal.valueOf(-rateThreshold)) < 0;
        }
        return false;
    }

    private boolean evaluateCombination(AlertRule rule, SignalDataContext context) {
        if (rule.getConditionExpr() == null || rule.getConditionExpr().isEmpty()) {
            return false;
        }
        try {
            Facts facts = new Facts();
            facts.put("signal", (Object)context);
            MVELRule mvelRule = new MVELRule().name(rule.getRuleName()).description(rule.getDescription()).when(rule.getConditionExpr()).then("System.out.println(\"Alert: \" + " + rule.getRuleName() + ");");
            Rules rules = new Rules(new Rule[0]);
            rules.register(new Object[]{mvelRule});
            this.rulesEngine.fire(rules, facts);
            return true;
        }
        catch (Exception e) {
            log.debug("MVEL rule evaluation failed: ruleId={}", (Object)rule.getId(), (Object)e);
            return false;
        }
    }

    private void handleAlert(AlertRule rule, SignalDataContext context) {
        this.updateCooldown(rule.getId(), context.getVin(), rule.getCooldownSec());
        String message = this.formatAlertMessage(rule, context);
        AlertTriggerLog triggerLog = new AlertTriggerLog();
        triggerLog.setRuleId(rule.getId());
        triggerLog.setRuleName(rule.getRuleName());
        triggerLog.setVin(context.getVin());
        triggerLog.setVehicleId(context.getVehicleId());
        triggerLog.setSignalName(context.getSignalName());
        triggerLog.setSignalValue(context.getCurrentValue() != null ? context.getCurrentValue().toPlainString() : "");
        triggerLog.setConditionMatched(this.buildConditionDesc(rule, context));
        triggerLog.setAlertLevel(rule.getAlertLevel());
        triggerLog.setAlertMessage(message);
        triggerLog.setNotified(0);
        triggerLog.setTriggerTime(LocalDateTime.now());
        triggerLog.setCreateTime(LocalDateTime.now());
        this.alertTriggerLogMapper.insert(triggerLog);
        this.insertVehicleAlert(rule, context, message);
        log.warn("ALERT TRIGGERED: rule={}, vin={}, signal={}, value={}, level={}, message={}", new Object[]{rule.getRuleName(), context.getVin(), context.getSignalName(), context.getCurrentValue(), rule.getAlertLevel(), message});
    }

    private void insertVehicleAlert(AlertRule rule, SignalDataContext context, String message) {
        try {
            VehicleAlert alert = new VehicleAlert();
            alert.setVin(context.getVin());
            alert.setVehicleId(context.getVehicleId());
            alert.setComponentCode(rule.getComponentCode());
            alert.setEcuType(rule.getEcuType());
            alert.setAlertType(rule.getRuleType());
            alert.setMessage(message);
            alert.setStatus(0);
            alert.setAlertTime(LocalDateTime.now());
            alert.setDeleted(0);
            alert.setCreateTime(LocalDateTime.now());
            alert.setUpdateTime(LocalDateTime.now());
            this.vehicleAlertMapper.insert(alert);
        }
        catch (Exception e) {
            log.error("Failed to insert vehicle alert", (Throwable)e);
        }
    }

    private String formatAlertMessage(AlertRule rule, SignalDataContext context) {
        String template = rule.getAlertMessage();
        if (template == null || template.isEmpty()) {
            template = "\u4fe1\u53f7 {signalName} \u89e6\u53d1\u544a\u8b66\uff0c\u5f53\u524d\u503c: {currentValue}";
        }
        return template.replace("{signalName}", context.getSignalName() != null ? context.getSignalName() : "").replace("{currentValue}", context.getCurrentValue() != null ? context.getCurrentValue().toPlainString() : "").replace("{unit}", context.getUnit() != null ? context.getUnit() : "").replace("{vin}", context.getVin() != null ? context.getVin() : "").replace("{threshold}", rule.getUpperThreshold() != null ? String.valueOf(rule.getUpperThreshold()) : "");
    }

    private String buildConditionDesc(AlertRule rule, SignalDataContext context) {
        if ("THRESHOLD".equals(rule.getRuleType())) {
            return "threshold: upper=" + rule.getUpperThreshold() + ", lower=" + rule.getLowerThreshold() + ", actual=" + String.valueOf(context.getCurrentValue());
        }
        if ("TREND".equals(rule.getRuleType())) {
            return "trend: direction=" + rule.getTrendDirection() + ", rate=" + String.valueOf(context.getTrendChangeRate()) + "%";
        }
        return "condition: " + rule.getConditionExpr();
    }

    private SignalDataContext buildContext(String vin, Long vehicleId, String signalName, BigDecimal value, String messageName, AlertRule rule) {
        SignalDataContext ctx = new SignalDataContext();
        ctx.setVin(vin);
        ctx.setVehicleId(vehicleId);
        ctx.setSignalName(signalName);
        ctx.setCurrentValue(value);
        ctx.setMessageName(messageName);
        ctx.setTimestamp(LocalDateTime.now());
        ctx.setUpperThreshold(rule.getUpperThreshold() != null ? BigDecimal.valueOf(rule.getUpperThreshold()) : null);
        ctx.setLowerThreshold(rule.getLowerThreshold() != null ? BigDecimal.valueOf(rule.getLowerThreshold()) : null);
        ctx.setHistoryValues(this.getSignalHistory(vin, signalName));
        return ctx;
    }

    private void updateSignalHistory(String vin, String signalName, BigDecimal value) {
        String key = vin + ":" + signalName;
        LinkedList history = this.signalHistory.computeIfAbsent(key, k -> new LinkedList());
        history.addLast(value);
        while (history.size() > 60) {
            history.removeFirst();
        }
    }

    private List<BigDecimal> getSignalHistory(String vin, String signalName) {
        String key = vin + ":" + signalName;
        return new ArrayList<BigDecimal>(this.signalHistory.getOrDefault(key, new LinkedList()));
    }

    private int incrementHitCounter(Long ruleId, String vin) {
        String key = ruleId + ":" + vin;
        return this.hitCounter.merge(key, 1, Integer::sum);
    }

    private void resetHitCounter(Long ruleId, String vin) {
        String key = ruleId + ":" + vin;
        this.hitCounter.remove(key);
    }

    private boolean isInCooldown(Long ruleId, String vin) {
        String key = ruleId + ":" + vin;
        Long lastTrigger = this.cooldownMap.get(key);
        if (lastTrigger == null) {
            return false;
        }
        return System.currentTimeMillis() - lastTrigger < 60000L;
    }

    private void updateCooldown(Long ruleId, String vin, Integer cooldownSec) {
        String key = ruleId + ":" + vin;
        this.cooldownMap.put(key, System.currentTimeMillis());
    }
}

