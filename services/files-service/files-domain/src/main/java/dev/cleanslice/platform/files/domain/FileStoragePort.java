package dev.cleanslice.platform.files.domain;

import java.io.InputStream;
import java.util.UUID;

public interface FileStoragePort {
    void put(UUID fileId, InputStream content, long size, String contentType);
    InputStream get(UUID fileId);
    void delete(UUID fileId);
    String getPresignedReadUrl(UUID fileId, long ttlSeconds);
    boolean exists(UUID fileId);
}