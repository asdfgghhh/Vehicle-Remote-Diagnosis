/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.baomidou.mybatisplus.extension.plugins.pagination.Page
 *  com.baomidou.mybatisplus.extension.service.IService
 *  org.springframework.web.multipart.MultipartFile
 */
package com.vrd.dbc.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.vrd.dbc.entity.DbcFile;
import java.util.List;
import java.util.Map;
import org.springframework.web.multipart.MultipartFile;

public interface DbcFileService
extends IService<DbcFile> {
    public Page<DbcFile> page(Integer var1, Integer var2, String var3, Long var4);

    public DbcFile uploadAndParse(MultipartFile var1, Long var2, String var3, String var4, String var5);

    public String parseDbcFile(String var1);

    public List<String> getMessageNames(String var1);

    public List<Map<String, String>> getSignalDefinitions(String var1);

    public List<Map<String, String>> getSignalDetails(String var1);

    public void updateMetadata(Long var1, String var2, String var3);

    public void publish(Long var1);

    public void revoke(Long var1);

    public List<Map<String, String>> getSignalDetailsByFileId(Long var1);

    public void dispatchToVehicle(Long var1, Long var2);

    public void dispatchToVehicles(Long var1, List<Long> var2);
}

