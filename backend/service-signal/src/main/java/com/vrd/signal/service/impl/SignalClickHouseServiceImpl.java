package com.vrd.signal.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.vrd.common.clickhouse.ClickHouseHttpClient;
import com.vrd.common.clickhouse.ClickHouseProperties;
import com.vrd.common.exception.BusinessException;
import com.vrd.signal.dto.SignalPageResult;
import com.vrd.signal.entity.VehicleSignal;
import com.vrd.signal.service.SignalClickHouseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@ConditionalOnProperty(name = "clickhouse.enabled", havingValue = "true", matchIfMissing = true)
public class SignalClickHouseServiceImpl implements SignalClickHouseService {

    private static final DateTimeFormatter CH_DATETIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final String SELECT_COLUMNS = """
            SELECT id, vin, vehicle_id, signal_name, signal_value, numeric_value, unit,
                   timestamp, signal_time, message_name, message_id, create_time
            FROM vehicle_signal_records
            """;

    private final ClickHouseHttpClient clickHouseHttpClient;
    private final ClickHouseProperties properties;

    public SignalClickHouseServiceImpl(ClickHouseHttpClient clickHouseHttpClient,
                                       ClickHouseProperties properties) {
        this.clickHouseHttpClient = clickHouseHttpClient;
        this.properties = properties;
    }

    @Override
    public List<VehicleSignal> queryByTimeRange(String vin, Long vehicleId, LocalDateTime startTime, LocalDateTime endTime) {
        String where = buildWhereClause(vin, vehicleId, startTime, endTime, null);
        return querySignals(SELECT_COLUMNS + where + " ORDER BY signal_time ASC");
    }

    @Override
    public SignalPageResult queryByTimeRangePaged(String vin, Long vehicleId, LocalDateTime startTime, LocalDateTime endTime,
                                                  int current, int size) {
        int pageSize = Math.min(Math.max(size, 1), properties.getMaxPageSize());
        int pageCurrent = Math.max(current, 1);
        String where = buildWhereClause(vin, vehicleId, startTime, endTime, null);
        long total = clickHouseHttpClient.queryCount("SELECT count() FROM vehicle_signal_records" + where);
        int offset = (pageCurrent - 1) * pageSize;
        List<VehicleSignal> records = querySignals(
                SELECT_COLUMNS + where + " ORDER BY signal_time ASC LIMIT " + pageSize + " OFFSET " + offset);
        return SignalPageResult.of(records, total, pageCurrent, pageSize);
    }

    @Override
    public List<VehicleSignal> queryBySignalName(String vin, Long vehicleId, String signalName,
                                                 LocalDateTime startTime, LocalDateTime endTime) {
        String where = buildWhereClause(vin, vehicleId, startTime, endTime, signalName);
        return querySignals(SELECT_COLUMNS + where + " ORDER BY signal_time ASC");
    }

    @Override
    public VehicleSignal getById(Long id) {
        List<VehicleSignal> list = querySignals(
                SELECT_COLUMNS + " WHERE id = " + id + " LIMIT 1");
        return list.isEmpty() ? null : list.get(0);
    }

    private List<VehicleSignal> querySignals(String sql) {
        String body = clickHouseHttpClient.query(sql + " FORMAT JSONEachRow");
        if (!StringUtils.hasText(body)) {
            return List.of();
        }
        List<VehicleSignal> records = new ArrayList<>();
        for (String line : body.split("\n")) {
            if (!StringUtils.hasText(line)) {
                continue;
            }
            records.add(mapSignal(JSONObject.parseObject(line)));
        }
        return records;
    }

    private VehicleSignal mapSignal(JSONObject json) {
        VehicleSignal signal = new VehicleSignal();
        signal.setId(json.getLong("id"));
        signal.setVin(json.getString("vin"));
        signal.setVehicleId(json.getLong("vehicle_id"));
        signal.setSignalName(json.getString("signal_name"));
        signal.setSignalValue(json.getString("signal_value"));
        signal.setNumericValue(BigDecimal.valueOf(json.getDoubleValue("numeric_value")));
        signal.setUnit(json.getString("unit"));
        signal.setTimestamp(json.getLong("timestamp"));
        signal.setSignalTime(parseDateTime(json.getString("signal_time")));
        signal.setMessageName(json.getString("message_name"));
        signal.setMessageId(json.getInteger("message_id"));
        signal.setCreateTime(parseDateTime(json.getString("create_time")));
        return signal;
    }

    private LocalDateTime parseDateTime(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return LocalDateTime.parse(value.replace('T', ' ').substring(0, 19), CH_DATETIME);
    }

    private String buildWhereClause(String vin, Long vehicleId, LocalDateTime startTime, LocalDateTime endTime,
                                    String signalName) {
        if (startTime == null || endTime == null) {
            throw new BusinessException("查询时间范围不能为空");
        }
        if (startTime.isAfter(endTime)) {
            throw new BusinessException("开始时间不能晚于结束时间");
        }

        StringBuilder where = new StringBuilder(" WHERE signal_time >= ")
                .append(dateTimeLiteral(startTime))
                .append(" AND signal_time <= ")
                .append(dateTimeLiteral(endTime));

        String vehicleFilter = buildVehicleFilter(vin, vehicleId);
        if (vehicleFilter != null) {
            where.append(" AND ").append(vehicleFilter);
        }
        if (StringUtils.hasText(signalName)) {
            where.append(" AND signal_name = ").append(ClickHouseHttpClient.literal(signalName.trim()));
        }
        return where.toString();
    }

    private String buildVehicleFilter(String vin, Long vehicleId) {
        if (StringUtils.hasText(vin)) {
            return "vin = " + ClickHouseHttpClient.literal(vin.trim());
        }
        if (vehicleId != null && vehicleId > 0) {
            return "vehicle_id = " + vehicleId;
        }
        return null;
    }

    private String dateTimeLiteral(LocalDateTime dateTime) {
        return "toDateTime(" + ClickHouseHttpClient.literal(dateTime.format(CH_DATETIME)) + ")";
    }
}
