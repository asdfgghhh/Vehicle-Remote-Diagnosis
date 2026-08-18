/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.web.multipart.MultipartFile
 */
package com.vrd.access.service;

import com.vrd.access.dto.EcuLogRecord;
import java.io.InputStream;
import java.time.LocalDateTime;
import org.springframework.web.multipart.MultipartFile;

public interface VehicleLogUploadService {
    public String initUpload(String var1, String var2, String var3, Long var4, String var5, LocalDateTime var6, LocalDateTime var7);

    public void uploadChunk(String var1, Integer var2, InputStream var3, Long var4);

    public EcuLogRecord completeUpload(String var1);

    public EcuLogRecord reportLog(MultipartFile var1, String var2, String var3, LocalDateTime var4, LocalDateTime var5, String var6);
}

