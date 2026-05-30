package com.vrd.ecu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("upload_chunk")
public class UploadChunk {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String chunkId;

    private String fileMd5;

    private Integer chunkNumber;

    private Long chunkSize;

    private String chunkPath;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
