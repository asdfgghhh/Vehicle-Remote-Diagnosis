/*
 * Decompiled with CFR 0.152.
 */
package com.vrd.dbc.parser;

import java.util.LinkedHashMap;
import java.util.Map;

public class DbcValueTable {
    private long messageId;
    private String signalName;
    private Map<Integer, String> entries;

    public String getDescription(int rawValue) {
        return this.entries.getOrDefault(rawValue, null);
    }

    public String getKey() {
        return this.messageId + ":" + this.signalName;
    }

    private static Map<Integer, String> $default$entries() {
        return new LinkedHashMap<Integer, String>();
    }

    public static DbcValueTableBuilder builder() {
        return new DbcValueTableBuilder();
    }

    public long getMessageId() {
        return this.messageId;
    }

    public String getSignalName() {
        return this.signalName;
    }

    public Map<Integer, String> getEntries() {
        return this.entries;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }

    public void setSignalName(String signalName) {
        this.signalName = signalName;
    }

    public void setEntries(Map<Integer, String> entries) {
        this.entries = entries;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof DbcValueTable)) {
            return false;
        }
        DbcValueTable other = (DbcValueTable)o;
        if (!other.canEqual(this)) {
            return false;
        }
        if (this.getMessageId() != other.getMessageId()) {
            return false;
        }
        String this$signalName = this.getSignalName();
        String other$signalName = other.getSignalName();
        if (this$signalName == null ? other$signalName != null : !this$signalName.equals(other$signalName)) {
            return false;
        }
        Map<Integer, String> this$entries = this.getEntries();
        Map<Integer, String> other$entries = other.getEntries();
        return !(this$entries == null ? other$entries != null : !((Object)this$entries).equals(other$entries));
    }

    protected boolean canEqual(Object other) {
        return other instanceof DbcValueTable;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        long $messageId = this.getMessageId();
        result = result * 59 + (int)($messageId >>> 32 ^ $messageId);
        String $signalName = this.getSignalName();
        result = result * 59 + ($signalName == null ? 43 : $signalName.hashCode());
        Map<Integer, String> $entries = this.getEntries();
        result = result * 59 + ($entries == null ? 43 : ((Object)$entries).hashCode());
        return result;
    }

    public String toString() {
        return "DbcValueTable(messageId=" + this.getMessageId() + ", signalName=" + this.getSignalName() + ", entries=" + String.valueOf(this.getEntries()) + ")";
    }

    public DbcValueTable() {
        this.entries = DbcValueTable.$default$entries();
    }

    public DbcValueTable(long messageId, String signalName, Map<Integer, String> entries) {
        this.messageId = messageId;
        this.signalName = signalName;
        this.entries = entries;
    }

    public static class DbcValueTableBuilder {
        private long messageId;
        private String signalName;
        private boolean entries$set;
        private Map<Integer, String> entries$value;

        DbcValueTableBuilder() {
        }

        public DbcValueTableBuilder messageId(long messageId) {
            this.messageId = messageId;
            return this;
        }

        public DbcValueTableBuilder signalName(String signalName) {
            this.signalName = signalName;
            return this;
        }

        public DbcValueTableBuilder entries(Map<Integer, String> entries) {
            this.entries$value = entries;
            this.entries$set = true;
            return this;
        }

        public DbcValueTable build() {
            Map<Integer, String> entries$value = this.entries$value;
            if (!this.entries$set) {
                entries$value = DbcValueTable.$default$entries();
            }
            return new DbcValueTable(this.messageId, this.signalName, entries$value);
        }

        public String toString() {
            return "DbcValueTable.DbcValueTableBuilder(messageId=" + this.messageId + ", signalName=" + this.signalName + ", entries$value=" + String.valueOf(this.entries$value) + ")";
        }
    }
}

