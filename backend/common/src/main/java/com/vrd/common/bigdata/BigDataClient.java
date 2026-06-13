package com.vrd.common.bigdata;

import com.alibaba.fastjson2.JSONObject;

import java.util.List;

public interface BigDataClient {

    void execute(String sql);

    void execute(String sql, String database);

    long queryCount(String sql);

    String query(String sql);

    String query(String sql, String database);

    <T> List<T> queryForList(String sql, Class<T> clazz);

    JSONObject queryForJson(String sql);

    boolean exists(String tableName, String condition);

    void insertJson(String tableName, List<JSONObject> rows);

    BigDataStorageType getStorageType();
}