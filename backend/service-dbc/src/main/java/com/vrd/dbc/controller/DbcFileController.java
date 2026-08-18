/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.baomidou.mybatisplus.extension.plugins.pagination.Page
 *  com.vrd.common.result.Result
 *  com.vrd.common.storage.StorageKeyUtils
 *  com.vrd.common.storage.StorageService
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.core.io.ByteArrayResource
 *  org.springframework.core.io.FileSystemResource
 *  org.springframework.core.io.Resource
 *  org.springframework.http.MediaType
 *  org.springframework.http.ResponseEntity
 *  org.springframework.http.ResponseEntity$BodyBuilder
 *  org.springframework.web.bind.annotation.DeleteMapping
 *  org.springframework.web.bind.annotation.GetMapping
 *  org.springframework.web.bind.annotation.PathVariable
 *  org.springframework.web.bind.annotation.PostMapping
 *  org.springframework.web.bind.annotation.PutMapping
 *  org.springframework.web.bind.annotation.RequestBody
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RequestParam
 *  org.springframework.web.bind.annotation.RestController
 *  org.springframework.web.multipart.MultipartFile
 */
package com.vrd.dbc.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vrd.common.result.Result;
import com.vrd.common.storage.StorageKeyUtils;
import com.vrd.common.storage.StorageService;
import com.vrd.dbc.entity.DbcFile;
import com.vrd.dbc.parser.CanFrameCodec;
import com.vrd.dbc.service.DbcFileService;
import com.vrd.dbc.service.impl.DbcFileServiceImpl;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(value={"/dbc"})
public class DbcFileController {
    @Autowired
    private DbcFileService dbcFileService;
    @Autowired
    private DbcFileServiceImpl dbcFileServiceImpl;
    @Autowired
    private StorageService storageService;

    @GetMapping(value={"/page"})
    public Result<Page<DbcFile>> page(@RequestParam(defaultValue="1") Integer current, @RequestParam(defaultValue="10") Integer size, @RequestParam(required=false) String keyword, @RequestParam(required=false) Long modelId) {
        Page<DbcFile> page = this.dbcFileService.page(current, size, keyword, modelId);
        return Result.success(page);
    }

    @GetMapping(value={"/{id}"})
    public Result<DbcFile> getById(@PathVariable Long id) {
        DbcFile dbcFile = (DbcFile)this.dbcFileService.getById(id);
        return Result.success((Object)dbcFile);
    }

    @PostMapping(value={"/upload"})
    public Result<DbcFile> upload(@RequestParam(value="file") MultipartFile file, @RequestParam Long modelId, @RequestParam(required=false) String modelName, @RequestParam(required=false) String version, @RequestParam(required=false) String description) {
        DbcFile result = this.dbcFileService.uploadAndParse(file, modelId, modelName, version, description);
        return Result.success((Object)result);
    }

    @GetMapping(value={"/{id}/messages"})
    public Result<List<String>> getMessages(@PathVariable Long id) {
        DbcFile dbcFile = (DbcFile)this.dbcFileService.getById(id);
        if (dbcFile == null) {
            return Result.error((String)"DBC\u6587\u4ef6\u4e0d\u5b58\u5728");
        }
        List<String> messages = this.dbcFileService.getMessageNames(dbcFile.getParseResult());
        return Result.success(messages);
    }

    @GetMapping(value={"/{id}/signals"})
    public Result<List<Map<String, String>>> getSignals(@PathVariable Long id) {
        DbcFile dbcFile = (DbcFile)this.dbcFileService.getById(id);
        if (dbcFile == null) {
            return Result.error((String)"DBC\u6587\u4ef6\u4e0d\u5b58\u5728");
        }
        return Result.success(this.dbcFileService.getSignalDefinitions(dbcFile.getParseResult()));
    }

    @GetMapping(value={"/{id}/signal-details"})
    public Result<List<Map<String, String>>> getSignalDetails(@PathVariable Long id) {
        DbcFile dbcFile = (DbcFile)this.dbcFileService.getById(id);
        if (dbcFile == null) {
            return Result.error((String)"DBC\u6587\u4ef6\u4e0d\u5b58\u5728");
        }
        return Result.success(this.dbcFileService.getSignalDetailsByFileId(id));
    }

    @PutMapping(value={"/{id}"})
    public Result<DbcFile> update(@PathVariable Long id, @RequestParam(required=false) String version, @RequestParam(required=false) String description) {
        this.dbcFileService.updateMetadata(id, version, description);
        return Result.success((Object)((DbcFile)this.dbcFileService.getById(id)));
    }

    @PostMapping(value={"/{id}/publish"})
    public Result<Void> publish(@PathVariable Long id) {
        this.dbcFileService.publish(id);
        return Result.success();
    }

    @PostMapping(value={"/{id}/revoke"})
    public Result<Void> revoke(@PathVariable Long id) {
        this.dbcFileService.revoke(id);
        return Result.success();
    }

    @DeleteMapping(value={"/{id}"})
    public Result<Void> delete(@PathVariable Long id) {
        DbcFile dbcFile = (DbcFile)this.dbcFileService.getById(id);
        if (dbcFile != null) {
            dbcFile.setDeleted(1);
            this.dbcFileService.updateById(dbcFile);
            this.dbcFileServiceImpl.evictParseCache(id);
        }
        return Result.success();
    }

    @GetMapping(value={"/{id}/download"})
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        FileSystemResource resource;
        long contentLength;
        DbcFile dbcFile;
        block10: {
            dbcFile = (DbcFile)this.dbcFileService.getById(id);
            if (dbcFile == null) {
                return ResponseEntity.notFound().build();
            }
            String objectKey = StorageKeyUtils.resolveObjectKey((String)dbcFile.getStorageKey(), (String)dbcFile.getFilePath(), (String)dbcFile.getStorageAddress(), (StorageService)this.storageService);
            long l = contentLength = dbcFile.getFileSize() != null ? dbcFile.getFileSize() : -1L;
            if (objectKey != null) {
                try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();){
                    this.storageService.download(objectKey, (OutputStream)outputStream);
                    byte[] data = outputStream.toByteArray();
                    resource = new ByteArrayResource(data);
                    contentLength = data.length;
                    break block10;
                }
                catch (Exception e) {
                    return ResponseEntity.internalServerError().build();
                }
            }
            File file = StorageKeyUtils.resolveLegacyLocalFile((String)dbcFile.getFilePath());
            if (file == null) {
                return ResponseEntity.notFound().build();
            }
            resource = new FileSystemResource(file);
            contentLength = file.length();
        }
        return ((ResponseEntity.BodyBuilder)ResponseEntity.ok().header("Content-Disposition", new String[]{"attachment; filename=\"" + dbcFile.getFileName() + "\""})).contentType(MediaType.APPLICATION_OCTET_STREAM).contentLength(contentLength).body((Object)resource);
    }

    @PostMapping(value={"/{id}/dispatch/{vehicleId}"})
    public Result<Void> dispatchToVehicle(@PathVariable Long id, @PathVariable Long vehicleId) {
        this.dbcFileService.dispatchToVehicle(id, vehicleId);
        return Result.success();
    }

    @PostMapping(value={"/{id}/dispatch"})
    public Result<Void> dispatchToVehicles(@PathVariable Long id, @RequestBody List<Long> vehicleIds) {
        this.dbcFileService.dispatchToVehicles(id, vehicleIds);
        return Result.success();
    }

    @GetMapping(value={"/{id}/structured"})
    public Result<Map<String, Object>> getStructured(@PathVariable Long id) {
        return Result.success(this.dbcFileServiceImpl.getStructuredData(id));
    }

    @GetMapping(value={"/{id}/message/{messageKey}"})
    public Result<Map<String, Object>> getMessageDetail(@PathVariable Long id, @PathVariable String messageKey) {
        return Result.success(this.dbcFileServiceImpl.getMessageDetailNative(id, messageKey));
    }

    @GetMapping(value={"/{id}/signals-native"})
    public Result<List<Map<String, Object>>> getSignalsNative(@PathVariable Long id, @RequestParam(required=false) String messageName) {
        return Result.success(this.dbcFileServiceImpl.getSignalsNative(id, messageName));
    }

    @PostMapping(value={"/{id}/decode"})
    public Result<Map<String, Object>> decodeCanFrame(@PathVariable Long id, @RequestParam long messageId, @RequestParam String dataHex) {
        return Result.success(this.dbcFileServiceImpl.decodeCanFrameNative(id, messageId, dataHex));
    }

    @PostMapping(value={"/{id}/encode"})
    public Result<Map<String, Object>> encodeCanFrame(@PathVariable Long id, @RequestParam long messageId, @RequestBody Map<String, Double> signalValues) {
        byte[] encoded = this.dbcFileServiceImpl.encodeCanFrameNative(id, messageId, signalValues);
        String hex = CanFrameCodec.formatHex(encoded);
        return Result.success(Map.of("messageId", messageId, "dataHex", hex, "length", encoded.length));
    }

    @GetMapping(value={"/{id}/generate/java-constants"})
    public Result<String> generateJavaConstants(@PathVariable Long id, @RequestParam(defaultValue="com.vrd.can") String packageName, @RequestParam(defaultValue="CanSignals") String className) {
        return Result.success((Object)this.dbcFileServiceImpl.generateJavaConstants(id, packageName, className));
    }

    @GetMapping(value={"/{id}/generate/json-schema"})
    public Result<String> generateJsonSchema(@PathVariable Long id) {
        return Result.success((Object)this.dbcFileServiceImpl.generateJsonSchema(id));
    }

    @DeleteMapping(value={"/{id}/cache"})
    public Result<Void> evictCache(@PathVariable Long id) {
        this.dbcFileServiceImpl.evictParseCache(id);
        return Result.success();
    }
}

