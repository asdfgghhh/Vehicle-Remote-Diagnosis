/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package com.vrd.dbc.parser;

import com.vrd.dbc.parser.DbcDatabase;
import com.vrd.dbc.parser.DbcMessage;
import com.vrd.dbc.parser.DbcSignal;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CanFrameCodec {
    private static final Logger log = LoggerFactory.getLogger(CanFrameCodec.class);
    private final DbcDatabase database;

    public CanFrameCodec(DbcDatabase database) {
        this.database = database;
    }

    public Map<String, DecodedSignal> decode(long messageId, byte[] data) {
        DbcMessage message = this.database.getMessageById(messageId);
        if (message == null) {
            log.warn("Unknown CAN ID: 0x{}", (Object)Long.toHexString(messageId));
            return Collections.emptyMap();
        }
        return this.decode(message, data);
    }

    public Map<String, DecodedSignal> decode(DbcMessage message, byte[] data) {
        LinkedHashMap<String, DecodedSignal> result = new LinkedHashMap<String, DecodedSignal>();
        for (DbcSignal signal : message.getSignals()) {
            try {
                DecodedSignal decoded = this.decodeSignal(signal, data);
                if (decoded == null) continue;
                result.put(signal.getName(), decoded);
            }
            catch (Exception e) {
                log.warn("Failed to decode signal {} in message 0x{}: {}", new Object[]{signal.getName(), Long.toHexString(message.getMessageId()), e.getMessage()});
            }
        }
        return result;
    }

    public DecodedSignal decodeSignal(DbcSignal signal, byte[] data) {
        long rawValue = this.extractRawValue(signal, data);
        double physicalValue = signal.rawToPhysical(rawValue);
        String choiceText = signal.getChoiceText((int)rawValue);
        return DecodedSignal.builder().name(signal.getName()).rawValue(rawValue).physicalValue(physicalValue).unit(signal.getUnit()).choiceText(choiceText).minimum(signal.getMinimum()).maximum(signal.getMaximum()).build();
    }

    public byte[] encode(long messageId, Map<String, Double> signalValues) {
        DbcMessage message = this.database.getMessageById(messageId);
        if (message == null) {
            throw new IllegalArgumentException("Unknown CAN ID: 0x" + Long.toHexString(messageId));
        }
        return this.encode(message, signalValues);
    }

    public byte[] encode(DbcMessage message, Map<String, Double> signalValues) {
        byte[] data = new byte[message.getLength()];
        Arrays.fill(data, (byte)0);
        for (DbcSignal signal : message.getSignals()) {
            Double physicalValue = signalValues.get(signal.getName());
            if (physicalValue == null) continue;
            long rawValue = Math.round(signal.physicalToRaw(physicalValue));
            this.writeRawValue(signal, data, rawValue);
        }
        return data;
    }

    private long extractRawValue(DbcSignal signal, byte[] data) {
        if (signal.getByteOrder() == DbcSignal.ByteOrder.LITTLE_ENDIAN) {
            return this.extractIntel(signal, data);
        }
        return this.extractMotorola(signal, data);
    }

    private long extractIntel(DbcSignal signal, byte[] data) {
        long signBit;
        int startBit = signal.getStartBit();
        int length = signal.getLength();
        long value = 0L;
        for (int i = 0; i < length; ++i) {
            int bitIndex = startBit + i;
            int byteIndex = bitIndex / 8;
            int bitInByte = bitIndex % 8;
            if (byteIndex >= data.length) continue;
            long bit = (long)(data[byteIndex] >> bitInByte) & 1L;
            value |= bit << i;
        }
        if (signal.isSigned() && length < 64 && (value & (signBit = 1L << length - 1)) != 0L) {
            value |= -1L << length;
        }
        if (length < 64 && signal.isSigned() && ((value &= (1L << length) - 1L) & 1L << length - 1) != 0L) {
            value |= (1L << length) - 1L ^ 0xFFFFFFFFFFFFFFFFL;
        }
        return value;
    }

    private long extractMotorola(DbcSignal signal, byte[] data) {
        long signBit;
        int startBit = signal.getStartBit();
        int length = signal.getLength();
        long value = 0L;
        for (int i = 0; i < length; ++i) {
            int bitIndex = startBit - i;
            int byteIndex = bitIndex / 8;
            int bitInByte = bitIndex % 8;
            if (byteIndex < 0 || byteIndex >= data.length) continue;
            long bit = (long)(data[byteIndex] >> bitInByte) & 1L;
            value |= bit << length - 1 - i;
        }
        if (signal.isSigned() && length < 64 && (value & (signBit = 1L << length - 1)) != 0L) {
            value |= (1L << length) - 1L ^ 0xFFFFFFFFFFFFFFFFL;
        }
        return value;
    }

    private void writeRawValue(DbcSignal signal, byte[] data, long rawValue) {
        if (signal.getByteOrder() == DbcSignal.ByteOrder.LITTLE_ENDIAN) {
            this.writeIntel(signal, data, rawValue);
        } else {
            this.writeMotorola(signal, data, rawValue);
        }
    }

    private void writeIntel(DbcSignal signal, byte[] data, long rawValue) {
        int bitInByte;
        int byteIndex;
        int bitIndex;
        int i;
        int startBit = signal.getStartBit();
        int length = signal.getLength();
        for (i = 0; i < length; ++i) {
            bitIndex = startBit + i;
            byteIndex = bitIndex / 8;
            bitInByte = bitIndex % 8;
            if (byteIndex >= data.length) continue;
            int n = byteIndex;
            data[n] = (byte)(data[n] & ~(1 << bitInByte));
        }
        for (i = 0; i < length; ++i) {
            bitIndex = startBit + i;
            byteIndex = bitIndex / 8;
            bitInByte = bitIndex % 8;
            if (byteIndex >= data.length) continue;
            long bit = rawValue >> i & 1L;
            int n = byteIndex;
            data[n] = (byte)((long)data[n] | bit << bitInByte);
        }
    }

    private void writeMotorola(DbcSignal signal, byte[] data, long rawValue) {
        int bitInByte;
        int byteIndex;
        int bitIndex;
        int i;
        int startBit = signal.getStartBit();
        int length = signal.getLength();
        for (i = 0; i < length; ++i) {
            bitIndex = startBit - i;
            byteIndex = bitIndex / 8;
            bitInByte = bitIndex % 8;
            if (byteIndex < 0 || byteIndex >= data.length) continue;
            int n = byteIndex;
            data[n] = (byte)(data[n] & ~(1 << bitInByte));
        }
        for (i = 0; i < length; ++i) {
            bitIndex = startBit - i;
            byteIndex = bitIndex / 8;
            bitInByte = bitIndex % 8;
            if (byteIndex < 0 || byteIndex >= data.length) continue;
            long bit = rawValue >> length - 1 - i & 1L;
            int n = byteIndex;
            data[n] = (byte)((long)data[n] | bit << bitInByte);
        }
    }

    public static String formatHex(byte[] data) {
        StringBuilder sb = new StringBuilder();
        for (byte b : data) {
            sb.append(String.format("%02X", b & 0xFF)).append(" ");
        }
        return sb.toString().trim();
    }

    public static byte[] parseHex(String hex) {
        hex = hex.replaceAll("\\s+", "");
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte)((Character.digit(hex.charAt(i), 16) << 4) + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }

    public static class DecodedSignal {
        private String name;
        private long rawValue;
        private double physicalValue;
        private String unit;
        private String choiceText;
        private double minimum;
        private double maximum;

        public static DecodedSignalBuilder builder() {
            return new DecodedSignalBuilder();
        }

        public String getName() {
            return this.name;
        }

        public long getRawValue() {
            return this.rawValue;
        }

        public double getPhysicalValue() {
            return this.physicalValue;
        }

        public String getUnit() {
            return this.unit;
        }

        public String getChoiceText() {
            return this.choiceText;
        }

        public double getMinimum() {
            return this.minimum;
        }

        public double getMaximum() {
            return this.maximum;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setRawValue(long rawValue) {
            this.rawValue = rawValue;
        }

        public void setPhysicalValue(double physicalValue) {
            this.physicalValue = physicalValue;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }

        public void setChoiceText(String choiceText) {
            this.choiceText = choiceText;
        }

        public void setMinimum(double minimum) {
            this.minimum = minimum;
        }

        public void setMaximum(double maximum) {
            this.maximum = maximum;
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof DecodedSignal)) {
                return false;
            }
            DecodedSignal other = (DecodedSignal)o;
            if (!other.canEqual(this)) {
                return false;
            }
            if (this.getRawValue() != other.getRawValue()) {
                return false;
            }
            if (Double.compare(this.getPhysicalValue(), other.getPhysicalValue()) != 0) {
                return false;
            }
            if (Double.compare(this.getMinimum(), other.getMinimum()) != 0) {
                return false;
            }
            if (Double.compare(this.getMaximum(), other.getMaximum()) != 0) {
                return false;
            }
            String this$name = this.getName();
            String other$name = other.getName();
            if (this$name == null ? other$name != null : !this$name.equals(other$name)) {
                return false;
            }
            String this$unit = this.getUnit();
            String other$unit = other.getUnit();
            if (this$unit == null ? other$unit != null : !this$unit.equals(other$unit)) {
                return false;
            }
            String this$choiceText = this.getChoiceText();
            String other$choiceText = other.getChoiceText();
            return !(this$choiceText == null ? other$choiceText != null : !this$choiceText.equals(other$choiceText));
        }

        protected boolean canEqual(Object other) {
            return other instanceof DecodedSignal;
        }

        public int hashCode() {
            int PRIME = 59;
            int result = 1;
            long $rawValue = this.getRawValue();
            result = result * 59 + (int)($rawValue >>> 32 ^ $rawValue);
            long $physicalValue = Double.doubleToLongBits(this.getPhysicalValue());
            result = result * 59 + (int)($physicalValue >>> 32 ^ $physicalValue);
            long $minimum = Double.doubleToLongBits(this.getMinimum());
            result = result * 59 + (int)($minimum >>> 32 ^ $minimum);
            long $maximum = Double.doubleToLongBits(this.getMaximum());
            result = result * 59 + (int)($maximum >>> 32 ^ $maximum);
            String $name = this.getName();
            result = result * 59 + ($name == null ? 43 : $name.hashCode());
            String $unit = this.getUnit();
            result = result * 59 + ($unit == null ? 43 : $unit.hashCode());
            String $choiceText = this.getChoiceText();
            result = result * 59 + ($choiceText == null ? 43 : $choiceText.hashCode());
            return result;
        }

        public String toString() {
            return "CanFrameCodec.DecodedSignal(name=" + this.getName() + ", rawValue=" + this.getRawValue() + ", physicalValue=" + this.getPhysicalValue() + ", unit=" + this.getUnit() + ", choiceText=" + this.getChoiceText() + ", minimum=" + this.getMinimum() + ", maximum=" + this.getMaximum() + ")";
        }

        public DecodedSignal() {
        }

        public DecodedSignal(String name, long rawValue, double physicalValue, String unit, String choiceText, double minimum, double maximum) {
            this.name = name;
            this.rawValue = rawValue;
            this.physicalValue = physicalValue;
            this.unit = unit;
            this.choiceText = choiceText;
            this.minimum = minimum;
            this.maximum = maximum;
        }

        public static class DecodedSignalBuilder {
            private String name;
            private long rawValue;
            private double physicalValue;
            private String unit;
            private String choiceText;
            private double minimum;
            private double maximum;

            DecodedSignalBuilder() {
            }

            public DecodedSignalBuilder name(String name) {
                this.name = name;
                return this;
            }

            public DecodedSignalBuilder rawValue(long rawValue) {
                this.rawValue = rawValue;
                return this;
            }

            public DecodedSignalBuilder physicalValue(double physicalValue) {
                this.physicalValue = physicalValue;
                return this;
            }

            public DecodedSignalBuilder unit(String unit) {
                this.unit = unit;
                return this;
            }

            public DecodedSignalBuilder choiceText(String choiceText) {
                this.choiceText = choiceText;
                return this;
            }

            public DecodedSignalBuilder minimum(double minimum) {
                this.minimum = minimum;
                return this;
            }

            public DecodedSignalBuilder maximum(double maximum) {
                this.maximum = maximum;
                return this;
            }

            public DecodedSignal build() {
                return new DecodedSignal(this.name, this.rawValue, this.physicalValue, this.unit, this.choiceText, this.minimum, this.maximum);
            }

            public String toString() {
                return "CanFrameCodec.DecodedSignal.DecodedSignalBuilder(name=" + this.name + ", rawValue=" + this.rawValue + ", physicalValue=" + this.physicalValue + ", unit=" + this.unit + ", choiceText=" + this.choiceText + ", minimum=" + this.minimum + ", maximum=" + this.maximum + ")";
            }
        }
    }
}

