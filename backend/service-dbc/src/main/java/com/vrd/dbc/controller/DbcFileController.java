package com.vrd.dbc.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vrd.common.result.Result;
import com.vrd.common.storage.StorageKeyUtils;
import com.vrd.common.storage.StorageService;
import com.vrd.dbc.entity.DbcFile;
import com.vrd.dbc.parser.CanFrameCodec;
import com.vrd.dbc.service.DbcFileService;
import com.vrd.dbc.service.impl.DbcFileServiceImpl;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * DBC 文件管理接口（服务端口 9084，网关路由前缀 /api/dbc）
 * <p>
 * 提供 DBC 文件的上传解析、报文/信号查询、CAN 帧编解码、
 * 版本发布与下发车辆、代码/Schema 生成等全生命周期能力。
 * 底层使用纯 Java 原生解析器（DbcParser + CanFrameCodec），精确支持 Intel/Motorola 字节序。
 */
@RestController
@RequestMapping("/dbc")
public class DbcFileController {

    @Autowired
    private DbcFileService dbcFileService;

    @Autowired
    private DbcFileServiceImpl dbcFileServiceImpl;

    @Autowired
    private StorageService storageService;

    /**
     * 分页查询 DBC 文件列表
     * <p>GET /dbc/page
     *
     * @param current 当前页码，默认 1
     * @param size    每页条数，默认 10
     * @param keyword 可选，按文件名模糊过滤
     * @param modelId 可选，按关联车型过滤
     * @return 分页结果 Page&lt;DbcFile&gt;
     */
    @GetMapping("/page")
    public Result<Page<DbcFile>> page(@RequestParam(defaultValue = "1") Integer current,
                                      @RequestParam(defaultValue = "10") Integer size,
                                      @RequestParam(required = false) String keyword,
                                      @RequestParam(required = false) Long modelId) {
        Page<DbcFile> page = this.dbcFileService.page(current, size, keyword, modelId);
        return Result.success(page);
    }

    /**
     * 按 ID 查询 DBC 文件元数据
     * <p>GET /dbc/{id}
     *
     * @param id DBC 文件 ID
     * @return DbcFile 文件元数据（不含报文明细）；不存在时 data 为 null
     */
    @GetMapping("/{id}")
    public Result<DbcFile> getById(@PathVariable Long id) {
        DbcFile dbcFile = this.dbcFileService.getById(id);
        return Result.success(dbcFile);
    }

    /**
     * 上传并解析 DBC 文件
     * <p>POST /dbc/upload（multipart/form-data）
     * <p>上传后立即解析 BO_/SG_ 主结构及 CM_/VAL_/BA_ 元数据，
     * 文件内容存入对象存储（MinIO），解析结果缓存于服务内存。
     *
     * @param file        DBC 文件（multipart 文件域）
     * @param modelId     关联车型 ID（必填）
     * @param modelName   可选，车型名称
     * @param version     可选，版本号
     * @param description 可选，备注描述
     * @return 解析入库后的 DbcFile 元数据
     */
    @PostMapping("/upload")
    public Result<DbcFile> upload(@RequestParam("file") MultipartFile file,
                                  @RequestParam Long modelId,
                                  @RequestParam(required = false) String modelName,
                                  @RequestParam(required = false) String version,
                                  @RequestParam(required = false) String description) {
        DbcFile result = this.dbcFileService.uploadAndParse(file, modelId, modelName, version, description);
        return Result.success(result);
    }

    /**
     * 查询 DBC 文件的报文（Message）名称列表
     * <p>GET /dbc/{id}/messages
     *
     * @param id DBC 文件 ID
     * @return List&lt;String&gt; 报文名称列表；文件不存在时返回错误
     */
    @GetMapping("/{id}/messages")
    public Result<List<String>> getMessages(@PathVariable Long id) {
        DbcFile dbcFile = this.dbcFileService.getById(id);
        if (dbcFile == null) {
            return Result.error("DBC文件不存在");
        }
        List<String> messages = this.dbcFileService.getMessageNames(dbcFile.getParseResult());
        return Result.success(messages);
    }

    /**
     * 查询 DBC 文件的信号（Signal）定义列表
     * <p>GET /dbc/{id}/signals
     *
     * @param id DBC 文件 ID
     * @return List&lt;Map&gt; 信号定义（名称、起始位、长度、字节序、缩放、偏移、单位等）
     */
    @GetMapping("/{id}/signals")
    public Result<List<Map<String, String>>> getSignals(@PathVariable Long id) {
        DbcFile dbcFile = this.dbcFileService.getById(id);
        if (dbcFile == null) {
            return Result.error("DBC文件不存在");
        }
        return Result.success(this.dbcFileService.getSignalDefinitions(dbcFile.getParseResult()));
    }

    /**
     * 查询 DBC 文件的信号明细（含元数据注释/值表）
     * <p>GET /dbc/{id}/signal-details
     *
     * @param id DBC 文件 ID
     * @return List&lt;Map&gt; 信号明细（含 CM_ 注释与 VAL_ 值表枚举）
     */
    @GetMapping("/{id}/signal-details")
    public Result<List<Map<String, String>>> getSignalDetails(@PathVariable Long id) {
        DbcFile dbcFile = this.dbcFileService.getById(id);
        if (dbcFile == null) {
            return Result.error("DBC文件不存在");
        }
        return Result.success(this.dbcFileService.getSignalDetailsByFileId(id));
    }

    /**
     * 更新 DBC 文件元数据（版本号/描述）
     * <p>PUT /dbc/{id}
     *
     * @param id          DBC 文件 ID
     * @param version     可选，新版本号
     * @param description 可选，新描述
     * @return 更新后的 DbcFile 元数据
     */
    @PutMapping("/{id}")
    public Result<DbcFile> update(@PathVariable Long id,
                                  @RequestParam(required = false) String version,
                                  @RequestParam(required = false) String description) {
        this.dbcFileService.updateMetadata(id, version, description);
        return Result.success(this.dbcFileService.getById(id));
    }

    /**
     * 发布 DBC 文件
     * <p>POST /dbc/{id}/publish
     * <p>发布后该文件对信号解析、编解码等消费方生效。
     *
     * @param id DBC 文件 ID
     * @return 空结果
     */
    @PostMapping("/{id}/publish")
    public Result<Void> publish(@PathVariable Long id) {
        this.dbcFileService.publish(id);
        return Result.success();
    }

    /**
     * 撤销发布 DBC 文件
     * <p>POST /dbc/{id}/revoke
     *
     * @param id DBC 文件 ID
     * @return 空结果
     */
    @PostMapping("/{id}/revoke")
    public Result<Void> revoke(@PathVariable Long id) {
        this.dbcFileService.revoke(id);
        return Result.success();
    }

    /**
     * 删除 DBC 文件（逻辑删除，置 deleted=1，并清除解析缓存）
     * <p>DELETE /dbc/{id}
     *
     * @param id DBC 文件 ID
     * @return 空结果；文件不存在时静默成功
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        DbcFile dbcFile = this.dbcFileService.getById(id);
        if (dbcFile != null) {
            dbcFile.setDeleted(1);
            this.dbcFileService.updateById(dbcFile);
            this.dbcFileServiceImpl.evictParseCache(id);
        }
        return Result.success();
    }

    /**
     * 下载 DBC 原始文件
     * <p>GET /dbc/{id}/download
     * <p>优先从对象存储（MinIO）读取，旧数据回退本地磁盘路径。
     *
     * @param id DBC 文件 ID
     * @return 二进制文件流（attachment，Content-Type: application/octet-stream）；不存在返回 404
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        DbcFile dbcFile = this.dbcFileService.getById(id);
        if (dbcFile == null) {
            return ResponseEntity.notFound().build();
        }
        String objectKey = StorageKeyUtils.resolveObjectKey(dbcFile.getStorageKey(), dbcFile.getFilePath(), dbcFile.getStorageAddress(), this.storageService);
        long contentLength = dbcFile.getFileSize() != null ? dbcFile.getFileSize() : -1L;
        Resource resource;
        if (objectKey != null) {
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                this.storageService.download(objectKey, outputStream);
                byte[] data = outputStream.toByteArray();
                resource = new ByteArrayResource(data);
                contentLength = data.length;
            } catch (Exception e) {
                return ResponseEntity.internalServerError().build();
            }
        } else {
            File file = StorageKeyUtils.resolveLegacyLocalFile(dbcFile.getFilePath());
            if (file == null) {
                return ResponseEntity.notFound().build();
            }
            resource = new FileSystemResource(file);
            contentLength = file.length();
        }
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + dbcFile.getFileName() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(contentLength)
                .body(resource);
    }

    /**
     * 按 DBC 文件关联的车型批量下发
     * <p>POST /dbc/{id}/dispatch
     * <p>根据 DBC 文件的 modelId 查询该车型下所有车辆，
     * 逐车创建 dispatch_log 并发送 Kafka 下发指令到车端。
     *
     * @param id DBC 文件 ID
     * @return Map 下发统计（total、sent、failed）
     */
    @PostMapping("/{id}/dispatch")
    public Result<Map<String, Object>> dispatch(@PathVariable Long id) {
        Map<String, Object> summary = this.dbcFileService.dispatchByModel(id);
        return Result.success(summary);
    }

    /**
     * 获取 DBC 文件的结构化数据（报文+信号树）
     * <p>GET /dbc/{id}/structured
     *
     * @param id DBC 文件 ID
     * @return Map 结构化数据（报文列表及其下属信号定义）
     */
    @GetMapping("/{id}/structured")
    public Result<Map<String, Object>> getStructured(@PathVariable Long id) {
        return Result.success(this.dbcFileServiceImpl.getStructuredData(id));
    }

    /**
     * 查询指定报文的明细
     * <p>GET /dbc/{id}/message/{messageKey}
     *
     * @param id         DBC 文件 ID
     * @param messageKey 报文标识（报文名称或 ID）
     * @return Map 报文属性及其信号列表
     */
    @GetMapping("/{id}/message/{messageKey}")
    public Result<Map<String, Object>> getMessageDetail(@PathVariable Long id, @PathVariable String messageKey) {
        return Result.success(this.dbcFileServiceImpl.getMessageDetailNative(id, messageKey));
    }

    /**
     * 原生方式查询信号列表
     * <p>GET /dbc/{id}/signals-native
     *
     * @param id          DBC 文件 ID
     * @param messageName 可选，按报文名称过滤；为空返回全部信号
     * @return List&lt;Map&gt; 信号原生定义（含物理值换算信息）
     */
    @GetMapping("/{id}/signals-native")
    public Result<List<Map<String, Object>>> getSignalsNative(@PathVariable Long id, @RequestParam(required = false) String messageName) {
        return Result.success(this.dbcFileServiceImpl.getSignalsNative(id, messageName));
    }

    /**
     * 解码 CAN 帧（原始十六进制数据 → 物理信号值）
     * <p>POST /dbc/{id}/decode?messageId={id}&amp;dataHex={hex}
     * <p>按 DBC 定义将 CAN 报文字节流解码为信号物理值，支持 Intel/Motorola 字节序。
     *
     * @param id        DBC 文件 ID
     * @param messageId CAN 报文 ID（十进制）
     * @param dataHex   CAN 帧数据（十六进制字符串，如 "0102AABBCCDDEEFF"）
     * @return Map 各信号的原始值与物理值
     */
    @PostMapping("/{id}/decode")
    public Result<Map<String, Object>> decodeCanFrame(@PathVariable Long id, @RequestParam long messageId, @RequestParam String dataHex) {
        return Result.success(this.dbcFileServiceImpl.decodeCanFrameNative(id, messageId, dataHex));
    }

    /**
     * 编码 CAN 帧（信号物理值 → 原始十六进制数据）
     * <p>POST /dbc/{id}/encode?messageId={id}
     * <p>将信号物理值按 DBC 定义（缩放/偏移/字节序）编码为 CAN 报文字节流。
     *
     * @param id           DBC 文件 ID
     * @param messageId    CAN 报文 ID（十进制）
     * @param signalValues 信号名到物理值的映射（JSON 请求体）
     * @return Map：messageId、dataHex（编码后的十六进制串）、length（字节数）
     */
    @PostMapping("/{id}/encode")
    public Result<Map<String, Object>> encodeCanFrame(@PathVariable Long id, @RequestParam long messageId, @RequestBody Map<String, Double> signalValues) {
        byte[] encoded = this.dbcFileServiceImpl.encodeCanFrameNative(id, messageId, signalValues);
        String hex = CanFrameCodec.formatHex(encoded);
        return Result.success(Map.of("messageId", messageId, "dataHex", hex, "length", encoded.length));
    }

    /**
     * 生成 Java 信号常量类源码
     * <p>GET /dbc/{id}/generate/java-constants
     * <p>将全部信号定义生成为 Java 常量类源码，便于车端/云端代码集成。
     *
     * @param id          DBC 文件 ID
     * @param packageName 可选，生成类的包名，默认 com.vrd.can
     * @param className   可选，生成类名，默认 CanSignals
     * @return String Java 源码文本
     */
    @GetMapping("/{id}/generate/java-constants")
    public Result<String> generateJavaConstants(@PathVariable Long id,
                                                @RequestParam(defaultValue = "com.vrd.can") String packageName,
                                                @RequestParam(defaultValue = "CanSignals") String className) {
        return Result.success(this.dbcFileServiceImpl.generateJavaConstants(id, packageName, className));
    }

    /**
     * 生成信号 JSON Schema
     * <p>GET /dbc/{id}/generate/json-schema
     *
     * @param id DBC 文件 ID
     * @return String 描述全部报文/信号的 JSON Schema 文本
     */
    @GetMapping("/{id}/generate/json-schema")
    public Result<String> generateJsonSchema(@PathVariable Long id) {
        return Result.success(this.dbcFileServiceImpl.generateJsonSchema(id));
    }

    /**
     * 手动清除 DBC 文件解析缓存
     * <p>DELETE /dbc/{id}/cache
     * <p>用于文件内容更新后缓存不一致时的人工干预，下次访问将重新解析。
     *
     * @param id DBC 文件 ID
     * @return 空结果
     */
    @DeleteMapping("/{id}/cache")
    public Result<Void> evictCache(@PathVariable Long id) {
        this.dbcFileServiceImpl.evictParseCache(id);
        return Result.success();
    }
}
