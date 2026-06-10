package com.vrd.access.service;

import com.vrd.access.model.UploadSession;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class UploadSessionStore {

    private final Map<String, UploadSession> sessions = new ConcurrentHashMap<>();

    public void save(UploadSession session) {
        sessions.put(session.getUploadId(), session);
    }

    public UploadSession get(String uploadId) {
        return sessions.get(uploadId);
    }

    public void remove(String uploadId) {
        sessions.remove(uploadId);
    }
}
