package dev.cleanslice.platform.files.application.port;

import java.io.InputStream;
import java.util.UUID;

/**
 * Output port for S3/MinIO storage operations.
 */
public interface StoragePort {
    void put(UUID fileId, InputStream content, long size, String contentType);
    InputStream get(UUID fileId);
    void delete(UUID fileId);
    String getPresignedReadUrl(UUID fileId, long ttlSeconds);
    boolean exists(UUID fileId);
}
