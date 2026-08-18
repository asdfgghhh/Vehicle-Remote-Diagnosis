/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vrd.common.bigdata.BigDataClient
 *  com.vrd.common.exception.BusinessException
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.stereotype.Service
 *  org.springframework.util.StringUtils
 */
package com.vrd.signal.service.impl;

import com.vrd.common.bigdata.BigDataClient;
import com.vrd.common.exception.BusinessException;
import com.vrd.signal.dto.SignalPageResult;
import com.vrd.signal.entity.VehicleSignal;
import com.vrd.signal.service.SignalClickHouseService;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class SignalClickHouseServiceImpl
implements SignalClickHouseService {
    private static final Logger log = LoggerFactory.getLogger(SignalClickHouseServiceImpl.class);
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String SELECT_COLUMNS = "SELECT id, vin, vehicle_id, signal_name, signal_value, numeric_value, unit,\n       timestamp, signal_time, message_name, message_id, create_time\nFROM vehicle_signal_records\n";
    private final BigDataClient bigDataClient;

    public SignalClickHouseServiceImpl(BigDataClient bigDataClient) {
        this.bigDataClient = bigDataClient;
    }

    @Override
    public List<VehicleSignal> queryByTimeRange(String vin, Long vehicleId, LocalDateTime startTime, LocalDateTime endTime) {
        String where = this.buildWhereClause(vin, vehicleId, startTime, endTime, null);
        return this.bigDataClient.queryForList(SELECT_COLUMNS + where + " ORDER BY signal_time ASC", VehicleSignal.class);
    }

    @Override
    public SignalPageResult queryByTimeRangePaged(String vin, Long vehicleId, LocalDateTime startTime, LocalDateTime endTime, int current, int size) {
        int pageSize = Math.min(Math.max(size, 1), 500);
        int pageCurrent = Math.max(current, 1);
        String where = this.buildWhereClause(vin, vehicleId, startTime, endTime, null);
        long total = this.bigDataClient.queryCount("SELECT count(*) FROM vehicle_signal_records" + where);
        int offset = (pageCurrent - 1) * pageSize;
        List records = this.bigDataClient.queryForList(SELECT_COLUMNS + where + " ORDER BY signal_time ASC LIMIT " + pageSize + " OFFSET " + offset, VehicleSignal.class);
        return SignalPageResult.of(records, total, pageCurrent, pageSize);
    }

    @Override
    public List<VehicleSignal> queryBySignalName(String vin, Long vehicleId, String signalName, LocalDateTime startTime, LocalDateTime endTime) {
        String where = this.buildWhereClause(vin, vehicleId, startTime, endTime, signalName);
        return this.bigDataClient.queryForList(SELECT_COLUMNS + where + " ORDER BY signal_time ASC", VehicleSignal.class);
    }

    @Override
    public VehicleSignal getById(Long id) {
        List list = this.bigDataClient.queryForList("SELECT id, vin, vehicle_id, signal_name, signal_value, numeric_value, unit,\n       timestamp, signal_time, message_name, message_id, create_time\nFROM vehicle_signal_records\n WHERE id = " + id + " LIMIT 1", VehicleSignal.class);
        return list.isEmpty() ? null : (VehicleSignal)list.get(0);
    }

    private String buildWhereClause(String vin, Long vehicleId, LocalDateTime startTime, LocalDateTime endTime, String signalName) {
        if (startTime == null || endTime == null) {
            throw new BusinessException("\u67e5\u8be2\u65f6\u95f4\u8303\u56f4\u4e0d\u80fd\u4e3a\u7a7a");
        }
        if (startTime.isAfter(endTime)) {
            throw new BusinessException("\u5f00\u59cb\u65f6\u95f4\u4e0d\u80fd\u665a\u4e8e\u7ed3\u675f\u65f6\u95f4");
        }
        StringBuilder where = new StringBuilder(" WHERE signal_time >= '").append(startTime.format(DATETIME_FORMAT)).append("' AND signal_time <= '").append(endTime.format(DATETIME_FORMAT)).append("'");
        String vehicleFilter = this.buildVehicleFilter(vin, vehicleId);
        if (vehicleFilter != null) {
            where.append(" AND ").append(vehicleFilter);
        }
        if (StringUtils.hasText((String)signalName)) {
            where.append(" AND signal_name = '").append(signalName.trim()).append("'");
        }
        return where.toString();
    }

    private String buildVehicleFilter(String vin, Long vehicleId) {
        if (StringUtils.hasText((String)vin)) {
            return "vin = '" + vin.trim() + "'";
        }
        if (vehicleId != null && vehicleId > 0L) {
            return "vehicle_id = " + vehicleId;
        }
        return null;
    }
}

