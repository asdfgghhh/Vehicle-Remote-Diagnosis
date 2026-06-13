package com.vrd.signal.service.impl;

import com.vrd.common.bigdata.BigDataClient;
import com.vrd.common.exception.BusinessException;
import com.vrd.signal.dto.SignalPageResult;
import com.vrd.signal.entity.VehicleSignal;
import com.vrd.signal.service.SignalClickHouseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
public class SignalClickHouseServiceImpl implements SignalClickHouseService {

    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final String SELECT_COLUMNS = """
            SELECT id, vin, vehicle_id, signal_name, signal_value, numeric_value, unit,
                   timestamp, signal_time, message_name, message_id, create_time
            FROM vehicle_signal_records
            """;

    private final BigDataClient bigDataClient;

    public SignalClickHouseServiceImpl(BigDataClient bigDataClient) {
        this.bigDataClient = bigDataClient;
    }

    @Override
    public List<VehicleSignal> queryByTimeRange(String vin, Long vehicleId, LocalDateTime startTime, LocalDateTime endTime) {
        String where = buildWhereClause(vin, vehicleId, startTime, endTime, null);
        return bigDataClient.queryForList(SELECT_COLUMNS + where + " ORDER BY signal_time ASC", VehicleSignal.class);
    }

    @Override
    public SignalPageResult queryByTimeRangePaged(String vin, Long vehicleId, LocalDateTime startTime, LocalDateTime endTime,
                                                  int current, int size) {
        int pageSize = Math.min(Math.max(size, 1), 500);
        int pageCurrent = Math.max(current, 1);
        String where = buildWhereClause(vin, vehicleId, startTime, endTime, null);
        long total = bigDataClient.queryCount("SELECT count(*) FROM vehicle_signal_records" + where);
        int offset = (pageCurrent - 1) * pageSize;
        List<VehicleSignal> records = bigDataClient.queryForList(
                SELECT_COLUMNS + where + " ORDER BY signal_time ASC LIMIT " + pageSize + " OFFSET " + offset, VehicleSignal.class);
        return SignalPageResult.of(records, total, pageCurrent, pageSize);
    }

    @Override
    public List<VehicleSignal> queryBySignalName(String vin, Long vehicleId, String signalName,
                                                 LocalDateTime startTime, LocalDateTime endTime) {
        String where = buildWhereClause(vin, vehicleId, startTime, endTime, signalName);
        return bigDataClient.queryForList(SELECT_COLUMNS + where + " ORDER BY signal_time ASC", VehicleSignal.class);
    }

    @Override
    public VehicleSignal getById(Long id) {
        List<VehicleSignal> list = bigDataClient.queryForList(
                SELECT_COLUMNS + " WHERE id = " + id + " LIMIT 1", VehicleSignal.class);
        return list.isEmpty() ? null : list.get(0);
    }

    private String buildWhereClause(String vin, Long vehicleId, LocalDateTime startTime, LocalDateTime endTime,
                                    String signalName) {
        if (startTime == null || endTime == null) {
            throw new BusinessException("查询时间范围不能为空");
        }
        if (startTime.isAfter(endTime)) {
            throw new BusinessException("开始时间不能晚于结束时间");
        }

        StringBuilder where = new StringBuilder(" WHERE signal_time >= '")
                .append(startTime.format(DATETIME_FORMAT))
                .append("' AND signal_time <= '")
                .append(endTime.format(DATETIME_FORMAT))
                .append("'");

        String vehicleFilter = buildVehicleFilter(vin, vehicleId);
        if (vehicleFilter != null) {
            where.append(" AND ").append(vehicleFilter);
        }
        if (StringUtils.hasText(signalName)) {
            where.append(" AND signal_name = '").append(signalName.trim()).append("'");
        }
        return where.toString();
    }

    private String buildVehicleFilter(String vin, Long vehicleId) {
        if (StringUtils.hasText(vin)) {
            return "vin = '" + vin.trim() + "'";
        }
        if (vehicleId != null && vehicleId > 0) {
            return "vehicle_id = " + vehicleId;
        }
        return null;
    }
}