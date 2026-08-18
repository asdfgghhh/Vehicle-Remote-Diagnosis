/*
 * Decompiled with CFR 0.152.
 */
package com.vrd.dbc.parser;

import java.util.LinkedHashMap;
import java.util.Map;

public class DbcSignal {
    private String name;
    private int startBit;
    private int length;
    private ByteOrder byteOrder;
    private boolean signed;
    private double factor;
    private double offset;
    private double minimum;
    private double maximum;
    private String unit;
    private String comment;
    private Map<Integer, String> choices;
    private long messageId;
    private String messageName;
    private String receivers;
    private boolean multiplexed;
    private int multiplexerSwitch;
    private String multiplexerMode;

    public double rawToPhysical(double rawValue) {
        return rawValue * this.factor + this.offset;
    }

    public double physicalToRaw(double physicalValue) {
        return (physicalValue - this.offset) / this.factor;
    }

    public String getChoiceText(int rawValue) {
        if (this.choices != null && this.choices.containsKey(rawValue)) {
            return this.choices.get(rawValue);
        }
        return null;
    }

    public Map<String, Object> toMap() {
        LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("name", this.name);
        map.put("startBit", this.startBit);
        map.put("length", this.length);
        map.put("byteOrder", this.byteOrder == ByteOrder.LITTLE_ENDIAN ? "Intel" : "Motorola");
        map.put("isSigned", this.signed);
        map.put("factor", this.factor);
        map.put("offset", this.offset);
        map.put("minimum", this.minimum);
        map.put("maximum", this.maximum);
        map.put("unit", this.unit != null ? this.unit : "");
        map.put("comment", this.comment != null ? this.comment : "");
        map.put("messageId", this.messageId);
        map.put("messageName", this.messageName);
        map.put("receivers", this.receivers != null ? this.receivers : "");
        map.put("choices", this.choices != null ? this.choices : new LinkedHashMap());
        map.put("multiplexed", this.multiplexed);
        return map;
    }

    public static DbcSignalBuilder builder() {
        return new DbcSignalBuilder();
    }

    public String getName() {
        return this.name;
    }

    public int getStartBit() {
        return this.startBit;
    }

    public int getLength() {
        return this.length;
    }

    public ByteOrder getByteOrder() {
        return this.byteOrder;
    }

    public boolean isSigned() {
        return this.signed;
    }

    public double getFactor() {
        return this.factor;
    }

    public double getOffset() {
        return this.offset;
    }

    public double getMinimum() {
        return this.minimum;
    }

    public double getMaximum() {
        return this.maximum;
    }

    public String getUnit() {
        return this.unit;
    }

    public String getComment() {
        return this.comment;
    }

    public Map<Integer, String> getChoices() {
        return this.choices;
    }

    public long getMessageId() {
        return this.messageId;
    }

    public String getMessageName() {
        return this.messageName;
    }

    public String getReceivers() {
        return this.receivers;
    }

    public boolean isMultiplexed() {
        return this.multiplexed;
    }

    public int getMultiplexerSwitch() {
        return this.multiplexerSwitch;
    }

    public String getMultiplexerMode() {
        return this.multiplexerMode;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStartBit(int startBit) {
        this.startBit = startBit;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public void setByteOrder(ByteOrder byteOrder) {
        this.byteOrder = byteOrder;
    }

    public void setSigned(boolean signed) {
        this.signed = signed;
    }

    public void setFactor(double factor) {
        this.factor = factor;
    }

    public void setOffset(double offset) {
        this.offset = offset;
    }

    public void setMinimum(double minimum) {
        this.minimum = minimum;
    }

    public void setMaximum(double maximum) {
        this.maximum = maximum;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setChoices(Map<Integer, String> choices) {
        this.choices = choices;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }

    public void setMessageName(String messageName) {
        this.messageName = messageName;
    }

    public void setReceivers(String receivers) {
        this.receivers = receivers;
    }

    public void setMultiplexed(boolean multiplexed) {
        this.multiplexed = multiplexed;
    }

    public void setMultiplexerSwitch(int multiplexerSwitch) {
        this.multiplexerSwitch = multiplexerSwitch;
    }

    public void setMultiplexerMode(String multiplexerMode) {
        this.multiplexerMode = multiplexerMode;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof DbcSignal)) {
            return false;
        }
        DbcSignal other = (DbcSignal)o;
        if (!other.canEqual(this)) {
            return false;
        }
        if (this.getStartBit() != other.getStartBit()) {
            return false;
        }
        if (this.getLength() != other.getLength()) {
            return false;
        }
        if (this.isSigned() != other.isSigned()) {
            return false;
        }
        if (Double.compare(this.getFactor(), other.getFactor()) != 0) {
            return false;
        }
        if (Double.compare(this.getOffset(), other.getOffset()) != 0) {
            return false;
        }
        if (Double.compare(this.getMinimum(), other.getMinimum()) != 0) {
            return false;
        }
        if (Double.compare(this.getMaximum(), other.getMaximum()) != 0) {
            return false;
        }
        if (this.getMessageId() != other.getMessageId()) {
            return false;
        }
        if (this.isMultiplexed() != other.isMultiplexed()) {
            return false;
        }
        if (this.getMultiplexerSwitch() != other.getMultiplexerSwitch()) {
            return false;
        }
        String this$name = this.getName();
        String other$name = other.getName();
        if (this$name == null ? other$name != null : !this$name.equals(other$name)) {
            return false;
        }
        ByteOrder this$byteOrder = this.getByteOrder();
        ByteOrder other$byteOrder = other.getByteOrder();
        if (this$byteOrder == null ? other$byteOrder != null : !((Object)((Object)this$byteOrder)).equals((Object)other$byteOrder)) {
            return false;
        }
        String this$unit = this.getUnit();
        String other$unit = other.getUnit();
        if (this$unit == null ? other$unit != null : !this$unit.equals(other$unit)) {
            return false;
        }
        String this$comment = this.getComment();
        String other$comment = other.getComment();
        if (this$comment == null ? other$comment != null : !this$comment.equals(other$comment)) {
            return false;
        }
        Map<Integer, String> this$choices = this.getChoices();
        Map<Integer, String> other$choices = other.getChoices();
        if (this$choices == null ? other$choices != null : !((Object)this$choices).equals(other$choices)) {
            return false;
        }
        String this$messageName = this.getMessageName();
        String other$messageName = other.getMessageName();
        if (this$messageName == null ? other$messageName != null : !this$messageName.equals(other$messageName)) {
            return false;
        }
        String this$receivers = this.getReceivers();
        String other$receivers = other.getReceivers();
        if (this$receivers == null ? other$receivers != null : !this$receivers.equals(other$receivers)) {
            return false;
        }
        String this$multiplexerMode = this.getMultiplexerMode();
        String other$multiplexerMode = other.getMultiplexerMode();
        return !(this$multiplexerMode == null ? other$multiplexerMode != null : !this$multiplexerMode.equals(other$multiplexerMode));
    }

    protected boolean canEqual(Object other) {
        return other instanceof DbcSignal;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        result = result * 59 + this.getStartBit();
        result = result * 59 + this.getLength();
        result = result * 59 + (this.isSigned() ? 79 : 97);
        long $factor = Double.doubleToLongBits(this.getFactor());
        result = result * 59 + (int)($factor >>> 32 ^ $factor);
        long $offset = Double.doubleToLongBits(this.getOffset());
        result = result * 59 + (int)($offset >>> 32 ^ $offset);
        long $minimum = Double.doubleToLongBits(this.getMinimum());
        result = result * 59 + (int)($minimum >>> 32 ^ $minimum);
        long $maximum = Double.doubleToLongBits(this.getMaximum());
        result = result * 59 + (int)($maximum >>> 32 ^ $maximum);
        long $messageId = this.getMessageId();
        result = result * 59 + (int)($messageId >>> 32 ^ $messageId);
        result = result * 59 + (this.isMultiplexed() ? 79 : 97);
        result = result * 59 + this.getMultiplexerSwitch();
        String $name = this.getName();
        result = result * 59 + ($name == null ? 43 : $name.hashCode());
        ByteOrder $byteOrder = this.getByteOrder();
        result = result * 59 + ($byteOrder == null ? 43 : ((Object)((Object)$byteOrder)).hashCode());
        String $unit = this.getUnit();
        result = result * 59 + ($unit == null ? 43 : $unit.hashCode());
        String $comment = this.getComment();
        result = result * 59 + ($comment == null ? 43 : $comment.hashCode());
        Map<Integer, String> $choices = this.getChoices();
        result = result * 59 + ($choices == null ? 43 : ((Object)$choices).hashCode());
        String $messageName = this.getMessageName();
        result = result * 59 + ($messageName == null ? 43 : $messageName.hashCode());
        String $receivers = this.getReceivers();
        result = result * 59 + ($receivers == null ? 43 : $receivers.hashCode());
        String $multiplexerMode = this.getMultiplexerMode();
        result = result * 59 + ($multiplexerMode == null ? 43 : $multiplexerMode.hashCode());
        return result;
    }

    public String toString() {
        return "DbcSignal(name=" + this.getName() + ", startBit=" + this.getStartBit() + ", length=" + this.getLength() + ", byteOrder=" + String.valueOf((Object)this.getByteOrder()) + ", signed=" + this.isSigned() + ", factor=" + this.getFactor() + ", offset=" + this.getOffset() + ", minimum=" + this.getMinimum() + ", maximum=" + this.getMaximum() + ", unit=" + this.getUnit() + ", comment=" + this.getComment() + ", choices=" + String.valueOf(this.getChoices()) + ", messageId=" + this.getMessageId() + ", messageName=" + this.getMessageName() + ", receivers=" + this.getReceivers() + ", multiplexed=" + this.isMultiplexed() + ", multiplexerSwitch=" + this.getMultiplexerSwitch() + ", multiplexerMode=" + this.getMultiplexerMode() + ")";
    }

    public DbcSignal() {
    }

    public DbcSignal(String name, int startBit, int length, ByteOrder byteOrder, boolean signed, double factor, double offset, double minimum, double maximum, String unit, String comment, Map<Integer, String> choices, long messageId, String messageName, String receivers, boolean multiplexed, int multiplexerSwitch, String multiplexerMode) {
        this.name = name;
        this.startBit = startBit;
        this.length = length;
        this.byteOrder = byteOrder;
        this.signed = signed;
        this.factor = factor;
        this.offset = offset;
        this.minimum = minimum;
        this.maximum = maximum;
        this.unit = unit;
        this.comment = comment;
        this.choices = choices;
        this.messageId = messageId;
        this.messageName = messageName;
        this.receivers = receivers;
        this.multiplexed = multiplexed;
        this.multiplexerSwitch = multiplexerSwitch;
        this.multiplexerMode = multiplexerMode;
    }

    public static enum ByteOrder {
        LITTLE_ENDIAN,
        BIG_ENDIAN;

    }

    public static class DbcSignalBuilder {
        private String name;
        private int startBit;
        private int length;
        private ByteOrder byteOrder;
        private boolean signed;
        private double factor;
        private double offset;
        private double minimum;
        private double maximum;
        private String unit;
        private String comment;
        private Map<Integer, String> choices;
        private long messageId;
        private String messageName;
        private String receivers;
        private boolean multiplexed;
        private int multiplexerSwitch;
        private String multiplexerMode;

        DbcSignalBuilder() {
        }

        public DbcSignalBuilder name(String name) {
            this.name = name;
            return this;
        }

        public DbcSignalBuilder startBit(int startBit) {
            this.startBit = startBit;
            return this;
        }

        public DbcSignalBuilder length(int length) {
            this.length = length;
            return this;
        }

        public DbcSignalBuilder byteOrder(ByteOrder byteOrder) {
            this.byteOrder = byteOrder;
            return this;
        }

        public DbcSignalBuilder signed(boolean signed) {
            this.signed = signed;
            return this;
        }

        public DbcSignalBuilder factor(double factor) {
            this.factor = factor;
            return this;
        }

        public DbcSignalBuilder offset(double offset) {
            this.offset = offset;
            return this;
        }

        public DbcSignalBuilder minimum(double minimum) {
            this.minimum = minimum;
            return this;
        }

        public DbcSignalBuilder maximum(double maximum) {
            this.maximum = maximum;
            return this;
        }

        public DbcSignalBuilder unit(String unit) {
            this.unit = unit;
            return this;
        }

        public DbcSignalBuilder comment(String comment) {
            this.comment = comment;
            return this;
        }

        public DbcSignalBuilder choices(Map<Integer, String> choices) {
            this.choices = choices;
            return this;
        }

        public DbcSignalBuilder messageId(long messageId) {
            this.messageId = messageId;
            return this;
        }

        public DbcSignalBuilder messageName(String messageName) {
            this.messageName = messageName;
            return this;
        }

        public DbcSignalBuilder receivers(String receivers) {
            this.receivers = receivers;
            return this;
        }

        public DbcSignalBuilder multiplexed(boolean multiplexed) {
            this.multiplexed = multiplexed;
            return this;
        }

        public DbcSignalBuilder multiplexerSwitch(int multiplexerSwitch) {
            this.multiplexerSwitch = multiplexerSwitch;
            return this;
        }

        public DbcSignalBuilder multiplexerMode(String multiplexerMode) {
            this.multiplexerMode = multiplexerMode;
            return this;
        }

        public DbcSignal build() {
            return new DbcSignal(this.name, this.startBit, this.length, this.byteOrder, this.signed, this.factor, this.offset, this.minimum, this.maximum, this.unit, this.comment, this.choices, this.messageId, this.messageName, this.receivers, this.multiplexed, this.multiplexerSwitch, this.multiplexerMode);
        }

        public String toString() {
            return "DbcSignal.DbcSignalBuilder(name=" + this.name + ", startBit=" + this.startBit + ", length=" + this.length + ", byteOrder=" + String.valueOf((Object)this.byteOrder) + ", signed=" + this.signed + ", factor=" + this.factor + ", offset=" + this.offset + ", minimum=" + this.minimum + ", maximum=" + this.maximum + ", unit=" + this.unit + ", comment=" + this.comment + ", choices=" + String.valueOf(this.choices) + ", messageId=" + this.messageId + ", messageName=" + this.messageName + ", receivers=" + this.receivers + ", multiplexed=" + this.multiplexed + ", multiplexerSwitch=" + this.multiplexerSwitch + ", multiplexerMode=" + this.multiplexerMode + ")";
        }
    }
}

