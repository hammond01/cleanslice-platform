package dev.cleanslice.platform.files.application.port;

import java.io.InputStream;
import java.util.UUID;

/**
 * Output port for S3/MinIO storage operations.
 */
public interface StoragePort {
    void put(String storageKey, InputStream content, long size, String contentType);
    InputStream get(String storageKey);
    void delete(String storageKey);
    String generatePresignedDownloadUrl(String storageKey);
    boolean exists(String storageKey);
}
