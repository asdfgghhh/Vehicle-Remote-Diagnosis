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
import com.vrd.dbc.parser.DbcNode;
import com.vrd.dbc.parser.DbcSignal;
import com.vrd.dbc.parser.DbcValueTable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DbcParser {
    private static final Logger log = LoggerFactory.getLogger(DbcParser.class);
    private static final Pattern VERSION_PATTERN = Pattern.compile("VERSION\\s+\"(.*)\"", 2);
    private static final Pattern BU_PATTERN = Pattern.compile("BU_:\\s*(.+)", 2);
    private static final Pattern BO_PATTERN = Pattern.compile("BO_\\s+(\\d+)\\s+(\\w+)\\s*:\\s*(\\d+)\\s+(\\w+)");
    private static final Pattern SG_PATTERN = Pattern.compile("SG_\\s+(\\w+)\\s*(M|m\\d+)?\\s*:\\s*(\\d+)\\|(\\d+)@(\\d)([+-])\\s+\\(([^,]+),([^)]+)\\)\\s*\\[([^|]*)\\|([^\\]]*)\\]\\s*\"([^\"]*)\"\\s*(.*)");
    private static final Pattern CM_BU_PATTERN = Pattern.compile("CM_\\s+BU_\\s+(\\w+)\\s+\"(.*)\";?");
    private static final Pattern CM_BO_PATTERN = Pattern.compile("CM_\\s+BO_\\s+(\\d+)\\s+\"(.*)\";?");
    private static final Pattern CM_SG_PATTERN = Pattern.compile("CM_\\s+SG_\\s+(\\d+)\\s+(\\w+)\\s+\"(.*)\";?");
    private static final Pattern VAL_PATTERN = Pattern.compile("VAL_\\s+(\\d+)\\s+(\\w+)\\s+(.+);");
    private static final Pattern VAL_ENTRY_PATTERN = Pattern.compile("(\\d+)\\s+\"([^\"]*)\"");
    private static final Pattern BA_DEF_PATTERN = Pattern.compile("BA_DEF_\\s+(?:\"(.*)\"\\s+)?(BU_|BO_|SG_|EV_)?\\s*(.*);");
    private static final Pattern BA_PATTERN = Pattern.compile("BA_\\s+\"([^\"]*)\"\\s+(BU_|BO_|SG_)\\s+(\\S+)\\s*(.*);");
    private static final Pattern BA_CYCLE_PATTERN = Pattern.compile("BA_\\s+\"GenMsgCycleTime\"\\s+BO_\\s+(\\d+)\\s+(\\d+);?");
    private static final Pattern BA_SEND_TYPE_PATTERN = Pattern.compile("BA_\\s+\"GenSigSendType\"\\s+SG_\\s+(\\d+)\\s+(\\w+)\\s*(.+);?");
    private static final Pattern SG_MUL_VAL_PATTERN = Pattern.compile("SG_MUL_VAL_\\s+(\\d+)\\s+(\\w+)\\s+(\\w+)\\s+(.+);?");
    private static final Pattern SIG_VALTYPE_PATTERN = Pattern.compile("SIG_VALTYPE_\\s+(\\d+)\\s+(\\w+)\\s*:\\s*(\\d+);?");
    private static final Pattern NODE_NAME_PATTERN = Pattern.compile("(\\w+)");
    private DbcDatabase database;
    private final Map<Long, DbcMessage> messageMap = new LinkedHashMap<Long, DbcMessage>();
    private final Map<String, DbcMessage> messageNameMap = new LinkedHashMap<String, DbcMessage>();
    private final List<DbcNode> nodes = new ArrayList<DbcNode>();
    private final Map<String, DbcValueTable> valueTables = new LinkedHashMap<String, DbcValueTable>();
    private final Map<String, String> pendingComments = new LinkedHashMap<String, String>();

    public DbcDatabase parse(InputStream inputStream) throws IOException {
        this.reset();
        List<String> lines = this.readAllLines(inputStream);
        this.parseMessagesAndSignals(lines);
        this.parseMetadata(lines);
        this.buildDatabase();
        log.info("DBC parsing complete: {} nodes, {} messages, {} signals", new Object[]{this.nodes.size(), this.database.getMessages().size(), this.database.getSignalCount()});
        return this.database;
    }

    public DbcDatabase parse(String dbcContent) {
        this.reset();
        List<String> lines = Arrays.asList(dbcContent.split("\\r?\\n"));
        this.parseMessagesAndSignals(lines);
        this.parseMetadata(lines);
        this.buildDatabase();
        return this.database;
    }

    private void parseMessagesAndSignals(List<String> lines) {
        DbcMessage currentMessage = null;
        for (String rawLine : lines) {
            DbcSignal signal;
            Matcher sgMatcher;
            String line = rawLine.trim();
            if (line.isEmpty() || line.startsWith("//") || currentMessage == null && !this.isKeywordLine(line)) continue;
            Matcher boMatcher = BO_PATTERN.matcher(line);
            if (boMatcher.find()) {
                long messageId = Long.parseLong(boMatcher.group(1));
                currentMessage = DbcMessage.builder().messageId(messageId).name(boMatcher.group(2)).length(Integer.parseInt(boMatcher.group(3))).sender(boMatcher.group(4)).signals(new ArrayList<DbcSignal>()).build();
                this.messageMap.put(messageId, currentMessage);
                this.messageNameMap.put(currentMessage.getName(), currentMessage);
                continue;
            }
            if (currentMessage == null || !(sgMatcher = SG_PATTERN.matcher(line)).find() || (signal = this.parseSignal(sgMatcher, currentMessage.getMessageId(), currentMessage.getName())) == null) continue;
            currentMessage.getSignals().add(signal);
        }
    }

    private DbcSignal parseSignal(Matcher m, long messageId, String messageName) {
        try {
            String signalName = m.group(1);
            String muxIndicator = m.group(2);
            int startBit = Integer.parseInt(m.group(3));
            int length = Integer.parseInt(m.group(4));
            int byteOrderInt = Integer.parseInt(m.group(5));
            boolean signed = "+".equals(m.group(6));
            double factor = this.parseDoubleSafe(m.group(7), 1.0);
            double offset = this.parseDoubleSafe(m.group(8), 0.0);
            double minimum = this.parseDoubleSafe(m.group(9), 0.0);
            double maximum = this.parseDoubleSafe(m.group(10), 0.0);
            String unit = m.group(11);
            String receivers = m.group(12) != null ? m.group(12).trim() : "";
            DbcSignal.DbcSignalBuilder builder = DbcSignal.builder().name(signalName).startBit(startBit).length(length).byteOrder(byteOrderInt == 1 ? DbcSignal.ByteOrder.LITTLE_ENDIAN : DbcSignal.ByteOrder.BIG_ENDIAN).signed(signed).factor(factor).offset(offset).minimum(minimum).maximum(maximum).unit(unit.isEmpty() ? null : unit).receivers(receivers).messageId(messageId).messageName(messageName).multiplexed(false).choices(new LinkedHashMap<Integer, String>());
            if (muxIndicator != null) {
                builder.multiplexed(true);
                if ("M".equals(muxIndicator)) {
                    builder.multiplexerMode("M");
                    builder.multiplexerSwitch(0);
                } else {
                    builder.multiplexerMode(muxIndicator);
                    builder.multiplexerSwitch(Integer.parseInt(muxIndicator.substring(1)));
                }
            }
            return builder.build();
        }
        catch (Exception e) {
            log.warn("Failed to parse signal line: {} : {}", (Object)m.group(0), (Object)e.getMessage());
            return null;
        }
    }

    private void parseMetadata(List<String> lines) {
        block0: for (String rawLine : lines) {
            Matcher vMatcher;
            String line = rawLine.trim();
            if (line.isEmpty() || line.startsWith("//") || (vMatcher = VERSION_PATTERN.matcher(line)).find() && this.database == null) continue;
            Matcher buMatcher = BU_PATTERN.matcher(line);
            if (buMatcher.find()) {
                String nodeList = buMatcher.group(1);
                Matcher nMatcher = NODE_NAME_PATTERN.matcher(nodeList);
                while (nMatcher.find()) {
                    this.nodes.add(DbcNode.builder().name(nMatcher.group(1)).build());
                }
                continue;
            }
            Matcher cmBuMatcher = CM_BU_PATTERN.matcher(line);
            if (cmBuMatcher.find()) {
                String nodeName = cmBuMatcher.group(1);
                String comment = cmBuMatcher.group(2);
                for (DbcNode node : this.nodes) {
                    if (!node.getName().equals(nodeName)) continue;
                    node.setComment(comment);
                    continue block0;
                }
                continue;
            }
            Matcher cmBoMatcher = CM_BO_PATTERN.matcher(line);
            if (cmBoMatcher.find()) {
                long msgId = Long.parseLong(cmBoMatcher.group(1));
                DbcMessage msg = this.messageMap.get(msgId);
                if (msg == null) continue;
                msg.setComment(cmBoMatcher.group(2));
                continue;
            }
            Matcher cmSgMatcher = CM_SG_PATTERN.matcher(line);
            if (cmSgMatcher.find()) {
                DbcSignal sig;
                long msgId = Long.parseLong(cmSgMatcher.group(1));
                String sigName = cmSgMatcher.group(2);
                String comment = cmSgMatcher.group(3);
                DbcMessage msg = this.messageMap.get(msgId);
                if (msg == null || (sig = msg.getSignal(sigName)) == null) continue;
                sig.setComment(comment);
                continue;
            }
            Matcher valMatcher = VAL_PATTERN.matcher(line);
            if (valMatcher.find()) {
                DbcSignal sig;
                long msgId = Long.parseLong(valMatcher.group(1));
                String sigName = valMatcher.group(2);
                String entriesStr = valMatcher.group(3);
                LinkedHashMap<Integer, String> entries = new LinkedHashMap<Integer, String>();
                Matcher entryMatcher = VAL_ENTRY_PATTERN.matcher(entriesStr);
                while (entryMatcher.find()) {
                    entries.put(Integer.parseInt(entryMatcher.group(1)), entryMatcher.group(2));
                }
                String key = msgId + ":" + sigName;
                DbcValueTable vt = DbcValueTable.builder().messageId(msgId).signalName(sigName).entries(entries).build();
                this.valueTables.put(key, vt);
                DbcMessage msg = this.messageMap.get(msgId);
                if (msg == null || (sig = msg.getSignal(sigName)) == null) continue;
                sig.setChoices(entries);
                continue;
            }
            Matcher baCycleMatcher = BA_CYCLE_PATTERN.matcher(line);
            if (!baCycleMatcher.find()) continue;
            long msgId = Long.parseLong(baCycleMatcher.group(1));
            int cycleMs = Integer.parseInt(baCycleMatcher.group(2));
            DbcMessage msg = this.messageMap.get(msgId);
            if (msg == null) continue;
            msg.setCycleTime(cycleMs);
        }
    }

    private void buildDatabase() {
        this.database = DbcDatabase.builder().version("").nodes(this.nodes).messages(new ArrayList<DbcMessage>()).messageById(new LinkedHashMap<Long, DbcMessage>()).messageByName(new LinkedHashMap<String, DbcMessage>()).valueTables(this.valueTables).attributes(new LinkedHashMap<String, String>()).build();
        for (DbcMessage msg : this.messageMap.values()) {
            this.database.addMessage(msg);
        }
    }

    private void reset() {
        this.database = null;
        this.messageMap.clear();
        this.messageNameMap.clear();
        this.nodes.clear();
        this.valueTables.clear();
        this.pendingComments.clear();
    }

    private List<String> readAllLines(InputStream inputStream) throws IOException {
        ArrayList<String> lines = new ArrayList<String>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));){
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }

    private boolean isKeywordLine(String line) {
        return line.startsWith("VERSION") || line.startsWith("BU_") || line.startsWith("BO_") || line.startsWith("SG_") || line.startsWith("CM_") || line.startsWith("VAL_") || line.startsWith("BA_DEF_") || line.startsWith("BA_") || line.startsWith("BS_") || line.startsWith("NS_") || line.startsWith("EV_") || line.startsWith("ENVVAR_DATA_") || line.startsWith("SIG_VALTYPE_") || line.startsWith("SIG_GROUP_") || line.startsWith("SG_MUL_VAL_");
    }

    private double parseDoubleSafe(String s, double defaultValue) {
        if (s == null || s.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(s.trim());
        }
        catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}

