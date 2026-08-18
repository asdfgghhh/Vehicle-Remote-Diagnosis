/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.stereotype.Service
 */
package com.vrd.dbc.service;

import com.vrd.dbc.parser.CanFrameCodec;
import com.vrd.dbc.parser.DbcDatabase;
import com.vrd.dbc.parser.DbcMessage;
import com.vrd.dbc.parser.DbcParser;
import com.vrd.dbc.parser.DbcSignal;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DbcParserService {
    private static final Logger log = LoggerFactory.getLogger(DbcParserService.class);
    private final Map<Long, DbcDatabase> databaseCache = new ConcurrentHashMap<Long, DbcDatabase>();
    private final Map<Long, CanFrameCodec> codecCache = new ConcurrentHashMap<Long, CanFrameCodec>();
    private final DbcParser parser = new DbcParser();

    public DbcDatabase parse(String dbcContent) {
        return this.parser.parse(dbcContent);
    }

    public DbcDatabase parse(InputStream inputStream) throws IOException {
        return this.parser.parse(inputStream);
    }

    public DbcDatabase parseAndCache(Long dbcId, InputStream inputStream) throws IOException {
        DbcDatabase db = this.parser.parse(inputStream);
        this.databaseCache.put(dbcId, db);
        this.codecCache.put(dbcId, new CanFrameCodec(db));
        log.info("DBC parsed and cached: dbcId={}, messages={}, signals={}", new Object[]{dbcId, db.getMessageCount(), db.getSignalCount()});
        return db;
    }

    public DbcDatabase parseAndCache(Long dbcId, String dbcContent) {
        DbcDatabase db = this.parser.parse(dbcContent);
        this.databaseCache.put(dbcId, db);
        this.codecCache.put(dbcId, new CanFrameCodec(db));
        return db;
    }

    public DbcDatabase getDatabase(Long dbcId) {
        return this.databaseCache.get(dbcId);
    }

    public CanFrameCodec getCodec(Long dbcId) {
        return this.codecCache.get(dbcId);
    }

    public Map<String, CanFrameCodec.DecodedSignal> decodeCanFrame(Long dbcId, long messageId, byte[] data) {
        CanFrameCodec codec = this.codecCache.get(dbcId);
        if (codec == null) {
            log.warn("DBC not cached: dbcId={}", (Object)dbcId);
            return Collections.emptyMap();
        }
        return codec.decode(messageId, data);
    }

    public Map<String, CanFrameCodec.DecodedSignal> decodeCanFrame(Long dbcId, long messageId, String dataHex) {
        byte[] data = CanFrameCodec.parseHex(dataHex);
        return this.decodeCanFrame(dbcId, messageId, data);
    }

    public void evictCache(Long dbcId) {
        this.databaseCache.remove(dbcId);
        this.codecCache.remove(dbcId);
        log.info("DBC cache evicted: dbcId={}", (Object)dbcId);
    }

    public void clearAllCache() {
        this.databaseCache.clear();
        this.codecCache.clear();
        log.info("All DBC cache cleared");
    }

    public Map<String, Integer> getCacheStats() {
        return Map.of("cachedDatabases", this.databaseCache.size(), "cachedCodecs", this.codecCache.size());
    }

    public String generateJavaConstants(DbcDatabase db, String packageName, String className) {
        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(packageName).append(";\n\n");
        sb.append("/**\n");
        sb.append(" * Auto-generated CAN signal constants from DBC database.\n");
        sb.append(" * DO NOT EDIT MANUALLY.\n");
        sb.append(" */\n");
        sb.append("public final class ").append(className).append(" {\n\n");
        sb.append("    private ").append(className).append("() {}\n\n");
        sb.append("    // ======== Message IDs ========\n\n");
        for (DbcMessage msg : db.getMessages()) {
            sb.append("    /** ").append(msg.getComment() != null ? msg.getComment() : msg.getName()).append(" */\n");
            sb.append("    public static final long MSG_").append(this.toJavaConstName(msg.getName())).append(" = 0x").append(Long.toHexString(msg.getMessageId()).toUpperCase()).append("L;\n\n");
        }
        sb.append("    // ======== Signal Names ========\n\n");
        for (DbcMessage msg : db.getMessages()) {
            sb.append("    // ").append(msg.getName()).append(" (0x").append(Long.toHexString(msg.getMessageId()).toUpperCase()).append(")\n");
            for (DbcSignal sig : msg.getSignals()) {
                sb.append("    public static final String SIG_").append(this.toJavaConstName(sig.getName())).append(" = \"").append(sig.getName()).append("\";\n");
            }
            sb.append("\n");
        }
        sb.append("}\n");
        return sb.toString();
    }

    public String generateJsonSchema(DbcDatabase db) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"$schema\": \"http://json-schema.org/draft-07/schema#\",\n");
        sb.append("  \"title\": \"CAN Signal Definitions\",\n");
        sb.append("  \"type\": \"object\",\n");
        sb.append("  \"properties\": {\n");
        for (DbcMessage msg : db.getMessages()) {
            sb.append("    \"").append(msg.getName()).append("\": {\n");
            sb.append("      \"type\": \"object\",\n");
            sb.append("      \"description\": \"").append(this.escapeJson(msg.getComment() != null ? msg.getComment() : "")).append("\",\n");
            sb.append("      \"canId\": \"0x").append(Long.toHexString(msg.getMessageId()).toUpperCase()).append("\",\n");
            sb.append("      \"dlc\": ").append(msg.getLength()).append(",\n");
            sb.append("      \"properties\": {\n");
            List<DbcSignal> signals = msg.getSignals();
            for (int i = 0; i < signals.size(); ++i) {
                DbcSignal sig = signals.get(i);
                sb.append("        \"").append(sig.getName()).append("\": {\n");
                sb.append("          \"type\": \"number\",\n");
                sb.append("          \"description\": \"").append(this.escapeJson(sig.getComment() != null ? sig.getComment() : "")).append("\",\n");
                sb.append("          \"unit\": \"").append(sig.getUnit() != null ? sig.getUnit() : "").append("\",\n");
                sb.append("          \"minimum\": ").append(sig.getMinimum()).append(",\n");
                sb.append("          \"maximum\": ").append(sig.getMaximum()).append(",\n");
                sb.append("          \"factor\": ").append(sig.getFactor()).append(",\n");
                sb.append("          \"offset\": ").append(sig.getOffset()).append(",\n");
                sb.append("          \"startBit\": ").append(sig.getStartBit()).append(",\n");
                sb.append("          \"length\": ").append(sig.getLength()).append(",\n");
                sb.append("          \"byteOrder\": \"").append(sig.getByteOrder() == DbcSignal.ByteOrder.LITTLE_ENDIAN ? "Intel" : "Motorola").append("\",\n");
                sb.append("          \"signed\": ").append(sig.isSigned()).append("\n");
                sb.append("        }").append(i < signals.size() - 1 ? "," : "").append("\n");
            }
            sb.append("      }\n");
            sb.append("    },\n");
        }
        int lastComma = sb.lastIndexOf(",");
        if (lastComma > 0) {
            sb.deleteCharAt(lastComma);
        }
        sb.append("  }\n");
        sb.append("}\n");
        return sb.toString();
    }

    private String toJavaConstName(String name) {
        return name.replaceAll("([a-z])([A-Z])", "$1_$2").replaceAll("[-.]", "_").toUpperCase();
    }

    private String escapeJson(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }
}

