package com.vrd.vehicle.dto;

import lombok.Data;

import java.util.List;

/**
 * 通用分页返回结果（与 ecu-log 模块保持一致结构，避免跨模块依赖）
 *
 * @param <T> 记录类型
 */
@Data
public class PageResult<T> {

    private List<T> records;
    private long total;
    private long current;
    private long size;

    public static <T> PageResult<T> of(List<T> records, long total, long current, long size) {
        PageResult<T> page = new PageResult<>();
        page.setRecords(records);
        page.setTotal(total);
        page.setCurrent(current);
        page.setSize(size);
        return page;
    }
}
