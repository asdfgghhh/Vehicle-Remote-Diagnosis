/*
 * Decompiled with CFR 0.152.
 */
package com.vrd.ecu.dto;

import java.util.List;

public class PageResult<T> {
    private List<T> records;
    private long total;
    private long current;
    private long size;

    public static <T> PageResult<T> of(List<T> records, long total, long current, long size) {
        PageResult<T> page = new PageResult<T>();
        page.setRecords(records);
        page.setTotal(total);
        page.setCurrent(current);
        page.setSize(size);
        return page;
    }

    public List<T> getRecords() {
        return this.records;
    }

    public long getTotal() {
        return this.total;
    }

    public long getCurrent() {
        return this.current;
    }

    public long getSize() {
        return this.size;
    }

    public void setRecords(List<T> records) {
        this.records = records;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public void setCurrent(long current) {
        this.current = current;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof PageResult)) {
            return false;
        }
        PageResult other = (PageResult)o;
        if (!other.canEqual(this)) {
            return false;
        }
        if (this.getTotal() != other.getTotal()) {
            return false;
        }
        if (this.getCurrent() != other.getCurrent()) {
            return false;
        }
        if (this.getSize() != other.getSize()) {
            return false;
        }
        List<T> this$records = this.getRecords();
        List<T> other$records = other.getRecords();
        return !(this$records == null ? other$records != null : !((Object)this$records).equals(other$records));
    }

    protected boolean canEqual(Object other) {
        return other instanceof PageResult;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        long $total = this.getTotal();
        result = result * 59 + (int)($total >>> 32 ^ $total);
        long $current = this.getCurrent();
        result = result * 59 + (int)($current >>> 32 ^ $current);
        long $size = this.getSize();
        result = result * 59 + (int)($size >>> 32 ^ $size);
        List<T> $records = this.getRecords();
        result = result * 59 + ($records == null ? 43 : ((Object)$records).hashCode());
        return result;
    }

    public String toString() {
        return "PageResult(records=" + String.valueOf(this.getRecords()) + ", total=" + this.getTotal() + ", current=" + this.getCurrent() + ", size=" + this.getSize() + ")";
    }
}

