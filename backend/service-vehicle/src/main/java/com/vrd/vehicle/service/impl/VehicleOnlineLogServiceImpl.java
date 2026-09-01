package com.vrd.vehicle.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.vrd.common.bigdata.BigDataClient;
import com.vrd.common.bigdata.BigDataStorageType;
import com.vrd.common.exception.BusinessException;
import com.vrd.common.utils.SnowflakeIdUtil;
import com.vrd.vehicle.dto.PageResult;
import com.vrd.vehicle.dto.VehicleOnlineLogRecord;
import com.vrd.vehicle.online.VehicleOnlineProperties;
import com.vrd.vehicle.service.VehicleOnlineLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 车辆在线状态变更日志服务实现（双库兼容：ClickHouse / TDengine）
 * <p>
 * 通过 BigDataClient#getStorageType 识别底层数据库，并使用对应方言构造 SQL：
 * <ul>
 *   <li>ClickHouse：直接使用列名（id, vin, vehicle_id 等），INSERT JSON 即可</li>
 *   <li>TDengine：写入时使用 INSERT INTO ... USING stable TAGS(vin) VALUES(...) 自动建子表，
 *       主键列为 event_time（TIMESTAMP）。故写入时构造独立 SQL 而非走 insertJson。</li>
 * </ul>
 * 经验 #948492：严禁在 CH 侧写 CREATE INDEX；性能依赖 PARTITION BY + ORDER BY。
 * 经验 #201464：写入入口必须统一（当前仅 insert/batchInsert 两条，future 扩展时保持一致）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleOnlineLogServiceImpl implements VehicleOnlineLogService {

    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private final BigDataClient bigDataClient;
    private final VehicleOnlineProperties properties;

    // ========================================================================
    // 写入
    // ========================================================================

    @Override
    public void insert(VehicleOnlineLogRecord record) {
        if (record == null) return;
        batchInsert(List.of(record));
    }

    @Override
    public void batchInsert(List<VehicleOnlineLogRecord> records) {
        if (records == null || records.isEmpty()) return;

        // 公共：补全 id / eventTime / createTime
        LocalDateTime now = LocalDateTime.now();
        for (VehicleOnlineLogRecord r : records) {
            if (r.getId() == null) r.setId(SnowflakeIdUtil.nextId());
            if (r.getEventTime() == null) r.setEventTime(now);
            if (r.getCreateTime() == null) r.setCreateTime(now);
        }

        BigDataStorageType type = bigDataClient.getStorageType();
        try {
            switch (type) {
                case CLICKHOUSE, DORIS -> writeViaInsertJson(records);
                case TDENGINE -> writeTdengineUsingTags(records);
                default -> writeViaInsertJson(records);
            }
        } catch (Exception e) {
            if (properties.isLogWriteDegradeOnError()) {
                log.warn("VehicleOnlineLog write failed(type={}), degrade: {}", type, e.getMessage());
            } else {
                throw new BusinessException("在线日志写入失败: " + e.getMessage());
            }
        }
    }

    /**
     * 走 BigDataClient.insertJson（ClickHouse / Doris 原生支持）
     * 经验 #201464：字段名需与建表 SQL 完全一致（下划线小列名）。
     */
    private void writeViaInsertJson(List<VehicleOnlineLogRecord> records) {
        String table = properties.getBigDataTableName();
        List<JSONObject> rows = new ArrayList<>(records.size());
        for (VehicleOnlineLogRecord r : records) {
            JSONObject row = new JSONObject();
            row.put("id", r.getId());
            row.put("vin", safe(r.getVin()));
            row.put("vehicle_id", r.getVehicleId() == null ? 0 : r.getVehicleId());
            row.put("status_from", r.getStatusFrom() == null ? -1 : r.getStatusFrom());
            row.put("status_to", r.getStatusTo() == null ? -1 : r.getStatusTo());
            row.put("reason", safe(r.getReason()));
            row.put("source", safe(r.getSource()));
            row.put("ext_info", safe(r.getExtInfo()));
            // CH/Doris 使用 DateTime64(3)，字符串写入带毫秒即可
            row.put("event_time", r.getEventTime().format(DATETIME_FMT));
            row.put("create_time", r.getCreateTime().format(DATETIME_FMT));
            rows.add(row);
        }
        bigDataClient.insertJson(table, rows);
    }

    /**
     * TDengine 写入：必须使用超级表 + TAGS 方式；event_time 为第一主键列。
     * <p>
     * TDengine RESTful 语法：
     * INSERT INTO tbl_vin USING stb_vehicle_online_log TAGS ('vin')
     * VALUES (event_time, id, vehicle_id, status_from, status_to, reason, source, ext_info, create_time);
     */
    private void writeTdengineUsingTags(List<VehicleOnlineLogRecord> records) {
        String tablePrefix = "vehicle_online_log_";
        String stable = properties.getTdengineStableName();
        StringBuilder sql = new StringBuilder(512 * records.size());
        for (VehicleOnlineLogRecord r : records) {
            String vin = safe(r.getVin());
            // 子表名不能包含非法字符，取 VIN 原样（VIN 本身为字母数字）
            String subTable = tablePrefix + sanitize(vin);

            sql.append("INSERT INTO ").append(subTable)
               .append(" USING ").append(stable).append(" TAGS ('").append(escape(vin)).append("') VALUES (");
            sql.append('\'').append(r.getEventTime().format(DATETIME_FMT)).append("', ");
            sql.append(r.getId() == null ? 0 : r.getId()).append(", ");
            sql.append(r.getVehicleId() == null ? 0 : r.getVehicleId()).append(", ");
            sql.append(r.getStatusFrom() == null ? -1 : r.getStatusFrom()).append(", ");
            sql.append(r.getStatusTo() == null ? -1 : r.getStatusTo()).append(", ");
            sql.append('\'').append(escape(safe(r.getReason()))).append("', ");
            sql.append('\'').append(escape(safe(r.getSource()))).append("', ");
            sql.append('\'').append(escape(safe(r.getExtInfo()))).append("', ");
            sql.append('\'').append(r.getCreateTime().format(DATETIME_FMT)).append("' ");
            sql.append("); ");
        }
        bigDataClient.execute(sql.toString());
    }

    // ========================================================================
    // 查询
    // ========================================================================

    @Override
    public PageResult<VehicleOnlineLogRecord> listByVin(String vin, Integer current, Integer size,
                                                        LocalDateTime startTime, LocalDateTime endTime) {
        if (!StringUtils.hasText(vin)) {
            throw new BusinessException("vin 不能为空");
        }
        long startMs = System.currentTimeMillis();
        int pageSize = Math.min(size == null ? 10 : size, 500);
        int pageCurrent = current == null || current < 1 ? 1 : current;
        LocalDateTime start = startTime != null ? startTime : LocalDateTime.now().minusDays(30);
        LocalDateTime end = endTime != null ? endTime : LocalDateTime.now();
        if (start.isAfter(end)) {
            throw new BusinessException("开始时间不能晚于结束时间");
        }

        String where = buildWhere(start, end, vin, null, null);
        int offset = (pageCurrent - 1) * pageSize;
        try {
            long total = bigDataClient.queryCount("SELECT count(*) FROM " + properties.getBigDataTableName() + where);
            String select = buildSelectColumns();
            List<VehicleOnlineLogRecord> records = bigDataClient.queryForList(
                    select + where + " ORDER BY event_time DESC LIMIT " + pageSize + " OFFSET " + offset,
                    VehicleOnlineLogRecord.class);
            long elapsed = System.currentTimeMillis() - startMs;
            log.info("VehicleOnlineLog listByVin {}ms, total={}, vin={}", elapsed, total, vin);
            return PageResult.of(records, total, pageCurrent, pageSize);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("VehicleOnlineLog listByVin failed, vin={}", vin, e);
            throw new BusinessException("在线日志查询失败: " + e.getMessage());
        }
    }

    @Override
    public List<Map<String, Object>> statDailyEvents(LocalDateTime startTime, LocalDateTime endTime) {
        LocalDateTime start = startTime != null ? startTime : LocalDateTime.now().minusDays(30);
        LocalDateTime end = endTime != null ? endTime : LocalDateTime.now();
        if (start.isAfter(end)) {
            throw new BusinessException("开始时间不能晚于结束时间");
        }
        String startStr = start.format(DATETIME_FMT);
        String endStr = end.format(DATETIME_FMT);

        BigDataStorageType type = bigDataClient.getStorageType();
        String sql;
        if (type == BigDataStorageType.TDENGINE) {
            // TDengine: INTERVAL 分桶
            sql = "SELECT status_to, COUNT(*) AS cnt " +
                  "FROM " + properties.getBigDataTableName() +
                  " WHERE event_time >= '" + startStr + "' AND event_time <= '" + endStr + "'" +
                  " INTERVAL(1d) PARTITION BY status_to";
        } else {
            // ClickHouse / Doris: toDate + GROUP BY
            sql = "SELECT toDate(event_time) AS statDate, " +
                  "       status_to, " +
                  "       count() AS cnt " +
                  "FROM " + properties.getBigDataTableName() +
                  " WHERE event_time >= '" + startStr + "' AND event_time <= '" + endStr + "'" +
                  " GROUP BY statDate, status_to ORDER BY statDate, status_to";
        }

        try {
            String raw = bigDataClient.query(sql);
            return parseStatDailyResult(raw, type);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("statDailyEvents failed", e);
            throw new BusinessException("每日事件统计失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> statVehicleRecentSummary(String vin, int days) {
        if (!StringUtils.hasText(vin)) {
            throw new BusinessException("vin 不能为空");
        }
        int d = Math.min(Math.max(days, 1), 365);
        String startStr = LocalDateTime.now().minusDays(d).format(DATETIME_FMT);
        String endStr = LocalDateTime.now().format(DATETIME_FMT);
        Map<String, Object> result = new HashMap<>();
        result.put("vin", vin);
        result.put("days", d);
        try {
            long online = bigDataClient.queryCount(
                    "SELECT count(*) FROM " + properties.getBigDataTableName() +
                    " WHERE vin = '" + escape(vin) + "' AND status_to = 1" +
                    " AND event_time >= '" + startStr + "' AND event_time <= '" + endStr + "'");
            long offline = bigDataClient.queryCount(
                    "SELECT count(*) FROM " + properties.getBigDataTableName() +
                    " WHERE vin = '" + escape(vin) + "' AND status_to = 0" +
                    " AND event_time >= '" + startStr + "' AND event_time <= '" + endStr + "'");
            result.put("onlineEvents", online);
            result.put("offlineEvents", offline);
        } catch (Exception e) {
            log.error("statVehicleRecentSummary failed, vin={}", vin, e);
            throw new BusinessException("车辆在线概览查询失败: " + e.getMessage());
        }
        return result;
    }

    // ========================================================================
    // 内部工具
    // ========================================================================

    private String buildSelectColumns() {
        BigDataStorageType type = bigDataClient.getStorageType();
        if (type == BigDataStorageType.TDENGINE) {
            // TDengine 列名：超级表直接用定义的字段名（event_time, id, vin...）
            // vin 作为 TAG 会和普通列一起出现
            return "SELECT event_time, id, vin, vehicle_id, status_from, status_to, reason, source, ext_info, create_time " +
                   "FROM " + properties.getBigDataTableName() + " ";
        }
        // ClickHouse / Doris 列名用下划线形式
        return "SELECT event_time eventTime, id, vin, vehicle_id vehicleId, status_from statusFrom, " +
               "       status_to statusTo, reason, source, ext_info extInfo, create_time createTime " +
               "FROM " + properties.getBigDataTableName() + " ";
    }

    private String buildWhere(LocalDateTime start, LocalDateTime end, String vin, Integer statusFrom, Integer statusTo) {
        StringBuilder sb = new StringBuilder(" WHERE ");
        BigDataStorageType type = bigDataClient.getStorageType();
        String et = type == BigDataStorageType.TDENGINE ? "event_time" : "event_time";
        sb.append(et).append(" >= '").append(start.format(DATETIME_FMT)).append("'");
        sb.append(" AND ").append(et).append(" <= '").append(end.format(DATETIME_FMT)).append("'");
        if (StringUtils.hasText(vin)) {
            sb.append(" AND vin = '").append(escape(vin.trim())).append("'");
        }
        if (statusFrom != null) {
            sb.append(" AND status_from = ").append(statusFrom);
        }
        if (statusTo != null) {
            sb.append(" AND status_to = ").append(statusTo);
        }
        return sb.toString();
    }

    private List<Map<String, Object>> parseStatDailyResult(String raw, BigDataStorageType type) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (!StringUtils.hasText(raw)) return result;
        if (type == BigDataStorageType.TDENGINE) {
            JSONObject resp = com.alibaba.fastjson2.JSON.parseObject(raw);
            com.alibaba.fastjson2.JSONArray data = resp.getJSONArray("data");
            if (data == null) return result;
            // TDengine 返回 [[ts, status_to, cnt]] 或 [status_to, cnt]
            // 简化：将列名/值按位置映射
            com.alibaba.fastjson2.JSONArray cols = resp.getJSONArray("column_meta");
            for (int i = 0; i < data.size(); i++) {
                com.alibaba.fastjson2.JSONArray row = data.getJSONArray(i);
                Map<String, Object> m = new HashMap<>();
                for (int c = 0; c < cols.size(); c++) {
                    String colName = cols.getJSONArray(c).getString(0);
                    // 统一 statDate 字段
                    if ("ts".equals(colName) || "event_time".equals(colName)) {
                        m.put("statDate", row.getString(c));
                    } else {
                        m.put(colName, row.get(c));
                    }
                }
                result.add(m);
            }
        } else {
            // ClickHouse JSONEachRow: { "statDate":..., "status_to":..., "cnt":... }
            List<JSONObject> list = com.alibaba.fastjson2.JSON.parseArray(raw, JSONObject.class);
            for (JSONObject j : list) {
                Map<String, Object> m = new HashMap<>(j);
                if (!m.containsKey("statDate") && m.containsKey("statdate")) {
                    m.put("statDate", m.get("statdate"));
                }
                result.add(m);
            }
        }
        return result;
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    private static String sanitize(String s) {
        if (s == null) return "NA";
        return s.replaceAll("[^a-zA-Z0-9_]", "_");
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("'", "\\'").replace("\\", "\\\\");
    }
}
