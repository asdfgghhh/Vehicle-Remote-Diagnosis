/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.stereotype.Component
 */
package com.vrd.access.service;

import com.vrd.access.model.UploadSession;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class UploadSessionStore {
    private final Map<String, UploadSession> sessions = new ConcurrentHashMap<String, UploadSession>();

    public void save(UploadSession session) {
        this.sessions.put(session.getUploadId(), session);
    }

    public UploadSession get(String uploadId) {
        return this.sessions.get(uploadId);
    }

    public void remove(String uploadId) {
        this.sessions.remove(uploadId);
    }
}

