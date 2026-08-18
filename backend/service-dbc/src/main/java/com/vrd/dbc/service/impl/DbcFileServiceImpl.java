/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.baomidou.mybatisplus.core.conditions.Wrapper
 *  com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper
 *  com.baomidou.mybatisplus.core.metadata.IPage
 *  com.baomidou.mybatisplus.extension.plugins.pagination.Page
 *  com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
 *  com.vrd.common.exception.BusinessException
 *  com.vrd.common.storage.StorageKeyUtils
 *  com.vrd.common.storage.StorageService
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.stereotype.Service
 *  org.springframework.web.multipart.MultipartFile
 */
package com.vrd.dbc.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vrd.common.exception.BusinessException;
import com.vrd.common.storage.StorageKeyUtils;
import com.vrd.common.storage.StorageService;
import com.vrd.dbc.entity.DbcFile;
import com.vrd.dbc.entity.DispatchLog;
import com.vrd.dbc.mapper.DbcFileMapper;
import com.vrd.dbc.mapper.DispatchLogMapper;
import com.vrd.dbc.parser.CanFrameCodec;
import com.vrd.dbc.parser.DbcDatabase;
import com.vrd.dbc.parser.DbcMessage;
import com.vrd.dbc.parser.DbcNode;
import com.vrd.dbc.parser.DbcSignal;
import com.vrd.dbc.service.DbcFileService;
import com.vrd.dbc.service.DbcParserService;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.invoke.CallSite;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DbcFileServiceImpl
extends ServiceImpl<DbcFileMapper, DbcFile>
implements DbcFileService {
    @Autowired
    private DispatchLogMapper dispatchLogMapper;
    @Autowired
    private StorageService storageService;
    @Autowired
    private DbcParserService dbcParserService;
    private static final Pattern MESSAGE_LINE = Pattern.compile("MESSAGE:\\s*BO_\\s*(\\d+)\\s+(\\w+):\\s*(\\d+)\\s+(\\w+)");
    private static final Pattern SIGNAL_LINE = Pattern.compile("SIGNAL:\\s+SG_\\s+(\\w+)\\s*:\\s*(\\d+)\\|(\\d+)@(\\d)([+-])\\s*\\(([^,]+),([^)]+)\\)\\s*\\[([^|]*)\\|([^\\]]*)\\]\\s*\"([^\"]*)\"\\s*(\\w+)");
    private static final Pattern COMMENT_LINE = Pattern.compile("COMMENT:\\s+CM_\\s+SG_\\s+(\\d+)\\s+(\\w+)\\s+\"(.*)\"");
    private static final Pattern VALUE_LINE = Pattern.compile("VALUE:\\s+VAL_\\s+(\\d+)\\s+(\\w+)\\s+(.+)");
    private static final Pattern MSG_CYCLE_LINE = Pattern.compile("MSG_CYCLE:\\s+(\\d+)\\s+(\\d+)");
    private static final Pattern SIG_SAMPLE_LINE = Pattern.compile("SIG_SAMPLE:\\s+(\\d+)\\s+(\\w+)\\s+(\\d+)");
    private static final Pattern RAW_MSG_CYCLE = Pattern.compile("BA_\\s+\"GenMsgCycleTime\"\\s+BO_\\s+(\\d+)\\s+(\\d+)");
    private static final Pattern RAW_SIG_SAMPLE = Pattern.compile("BA_\\s+\"SamplePeriod\"\\s+SG_\\s+(\\d+)\\s+(\\w+)\\s+(\\d+)");

    @Override
    public Page<DbcFile> page(Integer current, Integer size, String keyword, Long modelId) {
        Page page = new Page((long)current.intValue(), (long)size.intValue());
        LambdaQueryWrapper wrapper = new LambdaQueryWrapper();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(DbcFile::getFileName, (Object)keyword);
        }
        if (modelId != null) {
            wrapper.eq(DbcFile::getModelId, (Object)modelId);
        }
        wrapper.eq(DbcFile::getDeleted, (Object)0);
        wrapper.orderByDesc(DbcFile::getCreateTime);
        IPage result = this.page((IPage)page, (Wrapper)wrapper);
        return (Page)result;
    }

    @Override
    public DbcFile uploadAndParse(MultipartFile file, Long modelId, String modelName, String version, String description) {
        if (modelId == null) {
            throw new BusinessException("\u8bf7\u9009\u62e9\u8f66\u578b");
        }
        try {
            String dateStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
            String originalFilename = file.getOriginalFilename();
            String objectKey = "dbc/" + dateStr + "/" + originalFilename;
            String storageAddress = this.storageService.upload(objectKey, file.getInputStream(), file.getSize(), "application/octet-stream");
            String parseResult = this.parseWithNativeParser(file.getInputStream(), originalFilename);
            DbcFile dbcFile = new DbcFile();
            dbcFile.setModelId(modelId);
            dbcFile.setModelName(modelName);
            dbcFile.setFileName(originalFilename);
            dbcFile.setStorageKey(objectKey);
            dbcFile.setStorageAddress(storageAddress);
            dbcFile.setStorageType(this.storageService.getStorageType().name());
            dbcFile.setFilePath(objectKey);
            dbcFile.setFileSize(file.getSize());
            dbcFile.setVersion(version);
            dbcFile.setDescription(description);
            dbcFile.setParseResult(parseResult);
            dbcFile.setMessageCount(this.countMessages(parseResult));
            dbcFile.setSignalCount(this.countSignals(parseResult));
            dbcFile.setStatus(1);
            dbcFile.setDeleted(0);
            dbcFile.setCreateTime(LocalDateTime.now());
            dbcFile.setUpdateTime(LocalDateTime.now());
            this.save(dbcFile);
            try {
                InputStream cachedStream = this.openDbcInputStream(dbcFile);
                this.dbcParserService.parseAndCache(dbcFile.getId(), cachedStream);
                cachedStream.close();
            }
            catch (Exception e) {
                this.log.warn("Failed to cache DBC parse result: " + e.getMessage());
            }
            return dbcFile;
        }
        catch (IOException e) {
            throw new BusinessException("\u4e0a\u4f20DBC\u6587\u4ef6\u5931\u8d25: " + e.getMessage());
        }
    }

    private String parseWithNativeParser(InputStream inputStream, String originalFilename) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            inputStream.transferTo(baos);
            byte[] dbcData = baos.toByteArray();
            DbcDatabase db = this.dbcParserService.parse(new ByteArrayInputStream(dbcData));
            StringBuilder sb = new StringBuilder();
            sb.append("=== DBC Parser -- Java Native ===\n");
            sb.append("PARSER: com.vrd.dbc.parser.DbcParser v2.0\n");
            sb.append("FILE: ").append(originalFilename).append("\n");
            sb.append("MESSAGES: ").append(db.getMessageCount()).append("\n");
            sb.append("SIGNALS: ").append(db.getSignalCount()).append("\n");
            sb.append("NODES: ");
            for (DbcNode node : db.getNodes()) {
                sb.append(node.getName()).append(" ");
            }
            sb.append("\n\n");
            for (DbcMessage msg : db.getMessages()) {
                sb.append("MESSAGE: BO_ ").append(msg.getMessageId()).append(" ").append(msg.getName()).append(": ").append(msg.getLength()).append(" ").append(msg.getSender() != null ? msg.getSender() : "Vector__XXX").append("\n");
                if (msg.getCycleTime() != null) {
                    sb.append("  MSG_CYCLE: ").append(msg.getMessageId()).append(" ").append(msg.getCycleTime()).append("\n");
                }
                if (msg.getComment() != null && !msg.getComment().isEmpty()) {
                    sb.append("  COMMENT: CM_ BO_ ").append(msg.getMessageId()).append(" \"").append(msg.getComment()).append("\"\n");
                }
                for (DbcSignal sig : msg.getSignals()) {
                    sb.append("  SIGNAL: SG_ ").append(sig.getName());
                    if (sig.isMultiplexed()) {
                        sb.append(" ").append(sig.getMultiplexerMode());
                    }
                    sb.append(" : ").append(sig.getStartBit()).append("|").append(sig.getLength()).append("@").append(sig.getByteOrder() == DbcSignal.ByteOrder.LITTLE_ENDIAN ? "1" : "0").append(sig.isSigned() ? "+" : "-").append(" (").append(sig.getFactor()).append(",").append(sig.getOffset()).append(")").append(" [").append(sig.getMinimum()).append("|").append(sig.getMaximum()).append("]").append(" \"").append(sig.getUnit() != null ? sig.getUnit() : "").append("\"").append(" ").append(sig.getReceivers() != null ? sig.getReceivers() : "Vector__XXX").append("\n");
                    if (sig.getComment() != null && !sig.getComment().isEmpty()) {
                        sb.append("  COMMENT: CM_ SG_ ").append(msg.getMessageId()).append(" ").append(sig.getName()).append(" \"").append(sig.getComment()).append("\"\n");
                    }
                    if (sig.getChoices() == null || sig.getChoices().isEmpty()) continue;
                    sb.append("  VALUE: VAL_ ").append(msg.getMessageId()).append(" ").append(sig.getName()).append(" ");
                    sig.getChoices().forEach((k, v) -> sb.append(k).append(" \"").append((String)v).append("\" "));
                    sb.append(";\n");
                }
            }
            return sb.toString();
        }
        catch (Exception e) {
            this.log.error("Native DBC parser failed, falling back to regex: " + e.getMessage());
            return this.parseWithRegex(new ByteArrayInputStream(this.readAllBytes(inputStream)));
        }
    }

    private byte[] readAllBytes(InputStream is) {
        byte[] byArray;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            is.transferTo(baos);
            byArray = baos.toByteArray();
        }
        catch (Throwable throwable) {
            try {
                try {
                    baos.close();
                }
                catch (Throwable throwable2) {
                    throwable.addSuppressed(throwable2);
                }
                throw throwable;
            }
            catch (IOException e) {
                throw new BusinessException("\u8bfb\u53d6\u6587\u4ef6\u5931\u8d25");
            }
        }
        baos.close();
        return byArray;
    }

    @Override
    public String parseDbcFile(String filePath) {
        return this.parseWithRegex(this.openDbcInputStream(null, filePath, null));
    }

    private String parseWithRegex(InputStream inputStream) {
        StringBuilder parseResult = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));){
            String line;
            while ((line = reader.readLine()) != null) {
                if ((line = line.trim()).startsWith("BO_ ")) {
                    parseResult.append("MESSAGE: ").append(line).append("\n");
                    continue;
                }
                if (line.startsWith("SG_ ")) {
                    parseResult.append("  SIGNAL: ").append(line).append("\n");
                    continue;
                }
                if (line.startsWith("CM_ SG_")) {
                    parseResult.append("  COMMENT: ").append(line).append("\n");
                    continue;
                }
                if (line.startsWith("VAL_ ")) {
                    parseResult.append("  VALUE: ").append(line).append("\n");
                    continue;
                }
                if (!line.startsWith("BA_ ")) continue;
                this.appendCycleAttributes(parseResult, line);
            }
        }
        catch (IOException e) {
            throw new BusinessException("\u89e3\u6790DBC\u6587\u4ef6\u5931\u8d25: " + e.getMessage());
        }
        return parseResult.toString();
    }

    public Map<String, Object> decodeCanFrameNative(Long dbcId, long messageId, String dataHex) {
        try {
            DbcMessage message;
            DbcDatabase db = this.dbcParserService.getDatabase(dbcId);
            if (db == null) {
                DbcFile dbcFile = (DbcFile)this.getById(dbcId);
                if (dbcFile == null) {
                    return Map.of("error", "DBC \u6587\u4ef6\u4e0d\u5b58\u5728");
                }
                db = this.loadDbcIntoCache(dbcFile);
            }
            if ((message = db.getMessageById(messageId)) == null) {
                return Map.of("error", "\u672a\u77e5 CAN ID: 0x" + Long.toHexString(messageId));
            }
            Map<String, CanFrameCodec.DecodedSignal> decoded = this.dbcParserService.decodeCanFrame(dbcId, messageId, dataHex);
            LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>();
            result.put("messageId", messageId);
            result.put("messageName", message.getName());
            result.put("dataHex", dataHex);
            result.put("dbcId", dbcId);
            ArrayList signals = new ArrayList();
            for (CanFrameCodec.DecodedSignal ds : decoded.values()) {
                LinkedHashMap<String, Object> sigMap = new LinkedHashMap<String, Object>();
                sigMap.put("name", ds.getName());
                sigMap.put("rawValue", ds.getRawValue());
                sigMap.put("physicalValue", ds.getPhysicalValue());
                sigMap.put("unit", ds.getUnit());
                if (ds.getChoiceText() != null) {
                    sigMap.put("choiceText", ds.getChoiceText());
                }
                signals.add(sigMap);
            }
            result.put("signals", signals);
            return result;
        }
        catch (Exception e) {
            this.log.error("Native CAN decode failed", (Throwable)e);
            return Map.of("error", "\u89e3\u7801\u5931\u8d25: " + e.getMessage());
        }
    }

    public byte[] encodeCanFrameNative(Long dbcId, long messageId, Map<String, Double> signalValues) {
        CanFrameCodec codec;
        DbcDatabase db = this.dbcParserService.getDatabase(dbcId);
        if (db == null) {
            DbcFile dbcFile = (DbcFile)this.getById(dbcId);
            if (dbcFile == null) {
                throw new BusinessException("DBC \u6587\u4ef6\u4e0d\u5b58\u5728");
            }
            db = this.loadDbcIntoCache(dbcFile);
        }
        if ((codec = this.dbcParserService.getCodec(dbcId)) == null) {
            codec = new CanFrameCodec(db);
        }
        return codec.encode(messageId, signalValues);
    }

    public Map<String, Object> getStructuredData(Long dbcId) {
        DbcDatabase db = this.dbcParserService.getDatabase(dbcId);
        if (db == null) {
            DbcFile dbcFile = (DbcFile)this.getById(dbcId);
            if (dbcFile == null) {
                return Map.of("error", "DBC \u6587\u4ef6\u4e0d\u5b58\u5728");
            }
            db = this.loadDbcIntoCache(dbcFile);
        }
        return db.toSummaryMap();
    }

    public Map<String, Object> getMessageDetailNative(Long dbcId, String messageIdOrName) {
        DbcMessage msg;
        DbcDatabase db = this.dbcParserService.getDatabase(dbcId);
        if (db == null) {
            DbcFile dbcFile = (DbcFile)this.getById(dbcId);
            if (dbcFile == null) {
                return Map.of("error", "DBC \u6587\u4ef6\u4e0d\u5b58\u5728");
            }
            db = this.loadDbcIntoCache(dbcFile);
        }
        try {
            long id = Long.parseLong(messageIdOrName);
            msg = db.getMessageById(id);
        }
        catch (NumberFormatException e) {
            msg = db.getMessageByName(messageIdOrName);
        }
        if (msg == null) {
            return Map.of("error", "\u62a5\u6587\u4e0d\u5b58\u5728: " + messageIdOrName);
        }
        return msg.toMap();
    }

    public List<Map<String, Object>> getSignalsNative(Long dbcId, String messageIdOrName) {
        DbcDatabase db = this.dbcParserService.getDatabase(dbcId);
        if (db == null) {
            DbcFile dbcFile = (DbcFile)this.getById(dbcId);
            if (dbcFile == null) {
                return Collections.emptyList();
            }
            db = this.loadDbcIntoCache(dbcFile);
        }
        if (messageIdOrName != null && !messageIdOrName.isEmpty()) {
            DbcMessage msg;
            try {
                long id = Long.parseLong(messageIdOrName);
                msg = db.getMessageById(id);
            }
            catch (NumberFormatException e) {
                msg = db.getMessageByName(messageIdOrName);
            }
            if (msg == null) {
                return Collections.emptyList();
            }
            ArrayList<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
            for (DbcSignal sig : msg.getSignals()) {
                result.add(sig.toMap());
            }
            return result;
        }
        ArrayList<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        for (DbcSignal sig : db.getAllSignals()) {
            result.add(sig.toMap());
        }
        return result;
    }

    public String generateJavaConstants(Long dbcId, String packageName, String className) {
        DbcDatabase db = this.dbcParserService.getDatabase(dbcId);
        if (db == null) {
            DbcFile dbcFile = (DbcFile)this.getById(dbcId);
            if (dbcFile == null) {
                throw new BusinessException("DBC \u6587\u4ef6\u4e0d\u5b58\u5728");
            }
            db = this.loadDbcIntoCache(dbcFile);
        }
        return this.dbcParserService.generateJavaConstants(db, packageName, className);
    }

    public String generateJsonSchema(Long dbcId) {
        DbcDatabase db = this.dbcParserService.getDatabase(dbcId);
        if (db == null) {
            DbcFile dbcFile = (DbcFile)this.getById(dbcId);
            if (dbcFile == null) {
                throw new BusinessException("DBC \u6587\u4ef6\u4e0d\u5b58\u5728");
            }
            db = this.loadDbcIntoCache(dbcFile);
        }
        return this.dbcParserService.generateJsonSchema(db);
    }

    public void evictParseCache(Long dbcId) {
        this.dbcParserService.evictCache(dbcId);
    }

    private DbcDatabase loadDbcIntoCache(DbcFile dbcFile) {
        DbcDatabase dbcDatabase;
        block8: {
            InputStream is = this.openDbcInputStream(dbcFile);
            try {
                dbcDatabase = this.dbcParserService.parseAndCache(dbcFile.getId(), is);
                if (is == null) break block8;
            }
            catch (Throwable throwable) {
                try {
                    if (is != null) {
                        try {
                            is.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                catch (IOException e) {
                    throw new BusinessException("\u52a0\u8f7dDBC\u6587\u4ef6\u5931\u8d25: " + e.getMessage());
                }
            }
            is.close();
        }
        return dbcDatabase;
    }

    private InputStream openDbcInputStream(DbcFile dbcFile) {
        return this.openDbcInputStream(dbcFile.getStorageKey(), dbcFile.getFilePath(), dbcFile.getStorageAddress());
    }

    private InputStream openDbcInputStream(String storageKey, String filePath, String storageAddress) {
        String objectKey = StorageKeyUtils.resolveObjectKey((String)storageKey, (String)filePath, (String)storageAddress, (StorageService)this.storageService);
        if (objectKey != null) {
            return this.storageService.openInputStream(objectKey);
        }
        File legacyFile = StorageKeyUtils.resolveLegacyLocalFile((String)filePath);
        if (legacyFile != null) {
            try {
                return new FileInputStream(legacyFile);
            }
            catch (IOException e) {
                throw new BusinessException("\u8bfb\u53d6DBC\u6587\u4ef6\u5931\u8d25: " + e.getMessage());
            }
        }
        throw new BusinessException("DBC\u6587\u4ef6\u4e0d\u5b58\u5728\u6216\u5b58\u50a8\u5730\u5740\u65e0\u6548");
    }

    private void appendCycleAttributes(StringBuilder parseResult, String line) {
        Matcher msgCycleMatcher = RAW_MSG_CYCLE.matcher(line);
        if (msgCycleMatcher.find()) {
            parseResult.append("  MSG_CYCLE: ").append(msgCycleMatcher.group(1)).append(" ").append(msgCycleMatcher.group(2)).append("\n");
            return;
        }
        Matcher sigSampleMatcher = RAW_SIG_SAMPLE.matcher(line);
        if (sigSampleMatcher.find()) {
            parseResult.append("  SIG_SAMPLE: ").append(sigSampleMatcher.group(1)).append(" ").append(sigSampleMatcher.group(2)).append(" ").append(sigSampleMatcher.group(3)).append("\n");
        }
    }

    @Override
    public List<String> getMessageNames(String parseResult) {
        ArrayList<String> messages = new ArrayList<String>();
        Pattern pattern = Pattern.compile("MESSAGE:\\s*BO_\\s*\\d+\\s+(\\w+):");
        Matcher matcher = pattern.matcher(parseResult);
        while (matcher.find()) {
            messages.add(matcher.group(1));
        }
        return messages;
    }

    @Override
    public List<Map<String, String>> getSignalDefinitions(String parseResult) {
        ArrayList<Map<String, String>> signals = new ArrayList<Map<String, String>>();
        if (parseResult == null || parseResult.isEmpty()) {
            return signals;
        }
        String currentMessage = "";
        Pattern messagePattern = Pattern.compile("MESSAGE:\\s*BO_\\s*\\d+\\s+(\\w+):");
        Pattern signalPattern = Pattern.compile("SIGNAL:\\s+SG_\\s+(\\w+)");
        for (String line : parseResult.split("\n")) {
            Matcher messageMatcher = messagePattern.matcher(line);
            if (messageMatcher.find()) {
                currentMessage = messageMatcher.group(1);
                continue;
            }
            Matcher signalMatcher = signalPattern.matcher(line);
            if (!signalMatcher.find()) continue;
            HashMap<String, String> signal = new HashMap<String, String>();
            signal.put("name", signalMatcher.group(1));
            signal.put("messageName", currentMessage);
            signals.add(signal);
        }
        return signals;
    }

    @Override
    public List<Map<String, String>> getSignalDetails(String parseResult) {
        ArrayList<Map<String, String>> signals = new ArrayList<Map<String, String>>();
        if (parseResult == null || parseResult.isEmpty()) {
            return signals;
        }
        HashMap<CallSite, String> comments = new HashMap<CallSite, String>();
        HashMap<String, StringBuilder> valueTables = new HashMap<String, StringBuilder>();
        Map<String, String> messageCycles = this.buildMessageCycleMap(parseResult);
        Map<String, String> signalSamples = this.buildSignalSampleMap(parseResult);
        for (String line : parseResult.split("\n")) {
            Matcher commentMatcher = COMMENT_LINE.matcher(line.trim());
            if (commentMatcher.find()) {
                comments.put((CallSite)((Object)(commentMatcher.group(1) + ":" + commentMatcher.group(2))), commentMatcher.group(3));
                continue;
            }
            Matcher valueMatcher = VALUE_LINE.matcher(line.trim());
            if (!valueMatcher.find()) continue;
            String key = valueMatcher.group(1) + ":" + valueMatcher.group(2);
            valueTables.computeIfAbsent(key, k -> new StringBuilder()).append(valueMatcher.group(3)).append(" ");
        }
        String currentMessageId = "";
        String currentMessageName = "";
        for (String line : parseResult.split("\n")) {
            Matcher messageMatcher = MESSAGE_LINE.matcher(line.trim());
            if (messageMatcher.find()) {
                currentMessageId = messageMatcher.group(1);
                currentMessageName = messageMatcher.group(2);
                continue;
            }
            Matcher signalMatcher = SIGNAL_LINE.matcher(line.trim());
            if (!signalMatcher.find()) continue;
            String signalName = signalMatcher.group(1);
            String key = currentMessageId + ":" + signalName;
            HashMap<String, String> signal = new HashMap<String, String>();
            signal.put("name", signalName);
            signal.put("messageName", currentMessageName);
            signal.put("messageId", currentMessageId);
            signal.put("startBit", signalMatcher.group(2));
            signal.put("length", signalMatcher.group(3));
            signal.put("byteOrder", "1".equals(signalMatcher.group(4)) ? "Intel" : "Motorola");
            signal.put("signed", "+".equals(signalMatcher.group(5)) ? "\u65e0\u7b26\u53f7" : "\u6709\u7b26\u53f7");
            signal.put("factor", signalMatcher.group(6));
            signal.put("offset", signalMatcher.group(7));
            signal.put("min", signalMatcher.group(8));
            signal.put("max", signalMatcher.group(9));
            signal.put("unit", signalMatcher.group(10));
            signal.put("receiver", signalMatcher.group(11));
            signal.put("comment", comments.getOrDefault(key, ""));
            signal.put("valueDesc", this.formatValueDesc((StringBuilder)valueTables.get(key)));
            String sampleMs = this.resolveSamplePeriod(currentMessageId, signalName, messageCycles, signalSamples);
            signal.put("samplePeriodMs", sampleMs);
            signal.put("samplePeriod", this.formatSamplePeriod(sampleMs));
            signals.add(signal);
        }
        return signals;
    }

    @Override
    public List<Map<String, String>> getSignalDetailsByFileId(Long id) {
        DbcFile dbcFile = (DbcFile)this.getById(id);
        if (dbcFile == null || dbcFile.getDeleted() == 1) {
            throw new BusinessException("DBC\u6587\u4ef6\u4e0d\u5b58\u5728");
        }
        String parseResult = this.enrichParseResultWithCycles(dbcFile.getParseResult(), dbcFile);
        return this.getSignalDetails(parseResult);
    }

    @Override
    public void updateMetadata(Long id, String version, String description) {
        DbcFile dbcFile = (DbcFile)this.getById(id);
        if (dbcFile == null || dbcFile.getDeleted() == 1) {
            throw new BusinessException("DBC\u6587\u4ef6\u4e0d\u5b58\u5728");
        }
        if (version != null) {
            dbcFile.setVersion(version);
        }
        if (description != null) {
            dbcFile.setDescription(description);
        }
        dbcFile.setUpdateTime(LocalDateTime.now());
        this.updateById(dbcFile);
    }

    @Override
    public void publish(Long id) {
        DbcFile dbcFile = (DbcFile)this.getById(id);
        if (dbcFile == null || dbcFile.getDeleted() == 1) {
            throw new BusinessException("DBC\u6587\u4ef6\u4e0d\u5b58\u5728");
        }
        dbcFile.setStatus(2);
        dbcFile.setUpdateTime(LocalDateTime.now());
        this.updateById(dbcFile);
    }

    @Override
    public void revoke(Long id) {
        DbcFile dbcFile = (DbcFile)this.getById(id);
        if (dbcFile == null || dbcFile.getDeleted() == 1) {
            throw new BusinessException("DBC\u6587\u4ef6\u4e0d\u5b58\u5728");
        }
        dbcFile.setStatus(0);
        dbcFile.setUpdateTime(LocalDateTime.now());
        this.updateById(dbcFile);
    }

    @Override
    public void dispatchToVehicle(Long dbcFileId, Long vehicleId) {
        DbcFile dbcFile = (DbcFile)this.getById(dbcFileId);
        if (dbcFile == null) {
            throw new BusinessException("DBC\u6587\u4ef6\u4e0d\u5b58\u5728");
        }
        DispatchLog dispatchLog = new DispatchLog();
        dispatchLog.setDbcFileId(dbcFileId);
        dispatchLog.setVehicleId(vehicleId);
        dispatchLog.setDispatchType("SINGLE");
        dispatchLog.setStatus(1);
        dispatchLog.setDispatchTime(LocalDateTime.now());
        dispatchLog.setCreateTime(LocalDateTime.now());
        try {
            String result = this.sendToVehicle(StorageKeyUtils.resolveObjectKey((String)dbcFile.getStorageKey(), (String)dbcFile.getFilePath(), (String)dbcFile.getStorageAddress(), (StorageService)this.storageService), vehicleId);
            dispatchLog.setStatus(2);
            dispatchLog.setResult(result);
        }
        catch (Exception e) {
            dispatchLog.setStatus(3);
            dispatchLog.setResult("\u5931\u8d25: " + e.getMessage());
        }
        this.dispatchLogMapper.insert(dispatchLog);
    }

    @Override
    public void dispatchToVehicles(Long dbcFileId, List<Long> vehicleIds) {
        for (Long vehicleId : vehicleIds) {
            this.dispatchToVehicle(dbcFileId, vehicleId);
        }
    }

    private String sendToVehicle(String storageKey, Long vehicleId) {
        return "SUCCESS: DBC\u6587\u4ef6\u5df2\u4e0b\u53d1\u5230\u8f66\u8f86 " + vehicleId + ", key=" + storageKey;
    }

    private Map<String, String> buildMessageCycleMap(String parseResult) {
        HashMap<String, String> cycles = new HashMap<String, String>();
        for (String line : parseResult.split("\n")) {
            Matcher matcher = MSG_CYCLE_LINE.matcher(line.trim());
            if (!matcher.find()) continue;
            cycles.put(matcher.group(1), matcher.group(2));
        }
        return cycles;
    }

    private Map<String, String> buildSignalSampleMap(String parseResult) {
        HashMap<String, String> samples = new HashMap<String, String>();
        for (String line : parseResult.split("\n")) {
            Matcher matcher = SIG_SAMPLE_LINE.matcher(line.trim());
            if (!matcher.find()) continue;
            samples.put(matcher.group(1) + ":" + matcher.group(2), matcher.group(3));
        }
        return samples;
    }

    private String resolveSamplePeriod(String messageId, String signalName, Map<String, String> messageCycles, Map<String, String> signalSamples) {
        String signalKey = messageId + ":" + signalName;
        String sampleMs = signalSamples.get(signalKey);
        if (sampleMs == null || sampleMs.isEmpty()) {
            sampleMs = messageCycles.get(messageId);
        }
        return sampleMs == null ? "" : sampleMs;
    }

    private String formatSamplePeriod(String sampleMs) {
        if (sampleMs == null || sampleMs.isEmpty()) {
            return "";
        }
        return sampleMs + " ms";
    }

    private String formatValueDesc(StringBuilder raw) {
        if (raw == null || raw.isEmpty()) {
            return "";
        }
        String content = raw.toString().trim();
        if (content.endsWith(";")) {
            content = content.substring(0, content.length() - 1).trim();
        }
        Pattern pair = Pattern.compile("(\\d+)\\s+\"([^\"]*)\"");
        Matcher matcher = pair.matcher(content);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            if (result.length() > 0) {
                result.append("; ");
            }
            result.append(matcher.group(1)).append("=").append(matcher.group(2));
        }
        return result.length() > 0 ? result.toString() : content;
    }

    private int countMessages(String parseResult) {
        if (parseResult == null) {
            return 0;
        }
        int count = 0;
        Pattern pattern = Pattern.compile("BO_ \\d+");
        Matcher matcher = pattern.matcher(parseResult);
        while (matcher.find()) {
            ++count;
        }
        return count;
    }

    private int countSignals(String parseResult) {
        if (parseResult == null) {
            return 0;
        }
        int count = 0;
        Pattern pattern = Pattern.compile("SG_ \\w+");
        Matcher matcher = pattern.matcher(parseResult);
        while (matcher.find()) {
            ++count;
        }
        return count;
    }

    private String enrichParseResultWithCycles(String parseResult, DbcFile dbcFile) {
        StringBuilder enriched = new StringBuilder(parseResult == null ? "" : parseResult);
        try (InputStream inputStream = this.openDbcInputStream(dbcFile);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));){
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().startsWith("BA_ ")) continue;
                this.appendCycleAttributes(enriched, line.trim());
            }
        }
        catch (IOException e) {
            throw new BusinessException("\u8bfb\u53d6DBC\u91c7\u6837\u5468\u671f\u5931\u8d25: " + e.getMessage());
        }
        return enriched.toString();
    }
}

