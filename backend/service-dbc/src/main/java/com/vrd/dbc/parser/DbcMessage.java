/*
 * Decompiled with CFR 0.152.
 */
package com.vrd.dbc.parser;

import com.vrd.dbc.parser.DbcSignal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DbcMessage {
    private long messageId;
    private String name;
    private int length;
    private String sender;
    private String comment;
    private Integer cycleTime;
    private List<DbcSignal> signals;

    public DbcSignal getSignal(String signalName) {
        return this.signals.stream().filter(s -> s.getName().equals(signalName)).findFirst().orElse(null);
    }

    public List<DbcSignal> getMultiplexedSignals() {
        return this.signals.stream().filter(DbcSignal::isMultiplexed).toList();
    }

    public List<DbcSignal> getNormalSignals() {
        return this.signals.stream().filter(s -> !s.isMultiplexed()).toList();
    }

    public Map<String, Object> toMap() {
        LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("messageId", this.messageId);
        map.put("name", this.name);
        map.put("length", this.length);
        map.put("sender", this.sender != null ? this.sender : "Vector__XXX");
        map.put("comment", this.comment != null ? this.comment : "");
        map.put("cycleTime", this.cycleTime);
        map.put("signalCount", this.signals.size());
        ArrayList<Map<String, Object>> signalMaps = new ArrayList<Map<String, Object>>();
        for (DbcSignal signal : this.signals) {
            signalMaps.add(signal.toMap());
        }
        map.put("signals", signalMaps);
        return map;
    }

    private static List<DbcSignal> $default$signals() {
        return new ArrayList<DbcSignal>();
    }

    public static DbcMessageBuilder builder() {
        return new DbcMessageBuilder();
    }

    public long getMessageId() {
        return this.messageId;
    }

    public String getName() {
        return this.name;
    }

    public int getLength() {
        return this.length;
    }

    public String getSender() {
        return this.sender;
    }

    public String getComment() {
        return this.comment;
    }

    public Integer getCycleTime() {
        return this.cycleTime;
    }

    public List<DbcSignal> getSignals() {
        return this.signals;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setCycleTime(Integer cycleTime) {
        this.cycleTime = cycleTime;
    }

    public void setSignals(List<DbcSignal> signals) {
        this.signals = signals;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof DbcMessage)) {
            return false;
        }
        DbcMessage other = (DbcMessage)o;
        if (!other.canEqual(this)) {
            return false;
        }
        if (this.getMessageId() != other.getMessageId()) {
            return false;
        }
        if (this.getLength() != other.getLength()) {
            return false;
        }
        Integer this$cycleTime = this.getCycleTime();
        Integer other$cycleTime = other.getCycleTime();
        if (this$cycleTime == null ? other$cycleTime != null : !((Object)this$cycleTime).equals(other$cycleTime)) {
            return false;
        }
        String this$name = this.getName();
        String other$name = other.getName();
        if (this$name == null ? other$name != null : !this$name.equals(other$name)) {
            return false;
        }
        String this$sender = this.getSender();
        String other$sender = other.getSender();
        if (this$sender == null ? other$sender != null : !this$sender.equals(other$sender)) {
            return false;
        }
        String this$comment = this.getComment();
        String other$comment = other.getComment();
        if (this$comment == null ? other$comment != null : !this$comment.equals(other$comment)) {
            return false;
        }
        List<DbcSignal> this$signals = this.getSignals();
        List<DbcSignal> other$signals = other.getSignals();
        return !(this$signals == null ? other$signals != null : !((Object)this$signals).equals(other$signals));
    }

    protected boolean canEqual(Object other) {
        return other instanceof DbcMessage;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        long $messageId = this.getMessageId();
        result = result * 59 + (int)($messageId >>> 32 ^ $messageId);
        result = result * 59 + this.getLength();
        Integer $cycleTime = this.getCycleTime();
        result = result * 59 + ($cycleTime == null ? 43 : ((Object)$cycleTime).hashCode());
        String $name = this.getName();
        result = result * 59 + ($name == null ? 43 : $name.hashCode());
        String $sender = this.getSender();
        result = result * 59 + ($sender == null ? 43 : $sender.hashCode());
        String $comment = this.getComment();
        result = result * 59 + ($comment == null ? 43 : $comment.hashCode());
        List<DbcSignal> $signals = this.getSignals();
        result = result * 59 + ($signals == null ? 43 : ((Object)$signals).hashCode());
        return result;
    }

    public String toString() {
        return "DbcMessage(messageId=" + this.getMessageId() + ", name=" + this.getName() + ", length=" + this.getLength() + ", sender=" + this.getSender() + ", comment=" + this.getComment() + ", cycleTime=" + this.getCycleTime() + ", signals=" + String.valueOf(this.getSignals()) + ")";
    }

    public DbcMessage() {
        this.signals = DbcMessage.$default$signals();
    }

    public DbcMessage(long messageId, String name, int length, String sender, String comment, Integer cycleTime, List<DbcSignal> signals) {
        this.messageId = messageId;
        this.name = name;
        this.length = length;
        this.sender = sender;
        this.comment = comment;
        this.cycleTime = cycleTime;
        this.signals = signals;
    }

    public static class DbcMessageBuilder {
        private long messageId;
        private String name;
        private int length;
        private String sender;
        private String comment;
        private Integer cycleTime;
        private boolean signals$set;
        private List<DbcSignal> signals$value;

        DbcMessageBuilder() {
        }

        public DbcMessageBuilder messageId(long messageId) {
            this.messageId = messageId;
            return this;
        }

        public DbcMessageBuilder name(String name) {
            this.name = name;
            return this;
        }

        public DbcMessageBuilder length(int length) {
            this.length = length;
            return this;
        }

        public DbcMessageBuilder sender(String sender) {
            this.sender = sender;
            return this;
        }

        public DbcMessageBuilder comment(String comment) {
            this.comment = comment;
            return this;
        }

        public DbcMessageBuilder cycleTime(Integer cycleTime) {
            this.cycleTime = cycleTime;
            return this;
        }

        public DbcMessageBuilder signals(List<DbcSignal> signals) {
            this.signals$value = signals;
            this.signals$set = true;
            return this;
        }

        public DbcMessage build() {
            List<DbcSignal> signals$value = this.signals$value;
            if (!this.signals$set) {
                signals$value = DbcMessage.$default$signals();
            }
            return new DbcMessage(this.messageId, this.name, this.length, this.sender, this.comment, this.cycleTime, signals$value);
        }

        public String toString() {
            return "DbcMessage.DbcMessageBuilder(messageId=" + this.messageId + ", name=" + this.name + ", length=" + this.length + ", sender=" + this.sender + ", comment=" + this.comment + ", cycleTime=" + this.cycleTime + ", signals$value=" + String.valueOf(this.signals$value) + ")";
        }
    }
}

