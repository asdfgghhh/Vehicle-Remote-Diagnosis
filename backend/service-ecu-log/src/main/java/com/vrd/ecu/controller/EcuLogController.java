package com.vrd.ecu.controller;

import com.vrd.common.result.Result;
import com.vrd.ecu.dto.EcuLogRecord;
import com.vrd.ecu.dto.PageResult;
import com.vrd.ecu.service.EcuLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.time.LocalDateTime;

/**
 * ECU 日志接口（服务端口 9083，网关路由前缀 /api/ecu-log）
 * <p>
 * 提供车端 ECU 日志记录的分页查询与原始日志文件下载能力。
 * 日志文件由车端上传后经对象存储/本地磁盘管理。
 */
@RestController
@RequestMapping("/ecu-log")
public class EcuLogController {

    @Autowired
    private EcuLogService ecuLogService;

    /**
     * 分页查询 ECU 日志记录
     * <p>GET /ecu-log/page
     *
     * @param current   当前页码，默认 1
     * @param size      每页条数，默认 10
     * @param vin       可选，按车架号过滤
     * @param ecuType   可选，按 ECU 类型过滤
     * @param startTime 可选，采集时间范围起点（格式 yyyy-MM-dd HH:mm:ss）
     * @param endTime   可选，采集时间范围终点（格式 yyyy-MM-dd HH:mm:ss）
     * @return 分页结果 PageResult&lt;EcuLogRecord&gt;
     */
    @GetMapping("/page")
    public Result<PageResult<EcuLogRecord>> page(@RequestParam(value = "current", defaultValue = "1") Integer current,
                                                 @RequestParam(value = "size", defaultValue = "10") Integer size,
                                                 @RequestParam(value = "vin", required = false) String vin,
                                                 @RequestParam(value = "ecuType", required = false) String ecuType,
                                                 @RequestParam(value = "startTime", required = false)
                                                 @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
                                                 @RequestParam(value = "endTime", required = false)
                                                 @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        PageResult<EcuLogRecord> page = this.ecuLogService.page(current, size, vin, ecuType, startTime, endTime);
        return Result.success(page);
    }

    /**
     * 下载 ECU 原始日志文件
     * <p>GET /ecu-log/download/{id}
     *
     * @param id 日志记录 ID
     * @return 二进制文件流（attachment）；记录不存在或文件缺失返回 404
     */
    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> download(@PathVariable("id") Long id) {
        try {
            EcuLogRecord record = this.ecuLogService.getById(id);
            if (record == null) {
                return ResponseEntity.notFound().build();
            }
            File file = this.ecuLogService.downloadLog(id);
            FileSystemResource resource = new FileSystemResource(file);
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"" + record.getFileName() + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(file.length())
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
