/*
 * Decompiled with CFR 0.152.
 */
package com.vrd.signal.dto;

import com.vrd.signal.entity.VehicleSignal;
import java.util.List;

public class SignalPageResult {
    private List<VehicleSignal> records;
    private long total;
    private long current;
    private long size;

    public static SignalPageResult of(List<VehicleSignal> records, long total, long current, long size) {
        SignalPageResult result = new SignalPageResult();
        result.setRecords(records);
        result.setTotal(total);
        result.setCurrent(current);
        result.setSize(size);
        return result;
    }

    public List<VehicleSignal> getRecords() {
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

    public void setRecords(List<VehicleSignal> records) {
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
        if (!(o instanceof SignalPageResult)) {
            return false;
        }
        SignalPageResult other = (SignalPageResult)o;
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
        List<VehicleSignal> this$records = this.getRecords();
        List<VehicleSignal> other$records = other.getRecords();
        return !(this$records == null ? other$records != null : !((Object)this$records).equals(other$records));
    }

    protected boolean canEqual(Object other) {
        return other instanceof SignalPageResult;
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
        List<VehicleSignal> $records = this.getRecords();
        result = result * 59 + ($records == null ? 43 : ((Object)$records).hashCode());
        return result;
    }

    public String toString() {
        return "SignalPageResult(records=" + String.valueOf(this.getRecords()) + ", total=" + this.getTotal() + ", current=" + this.getCurrent() + ", size=" + this.getSize() + ")";
    }
}

