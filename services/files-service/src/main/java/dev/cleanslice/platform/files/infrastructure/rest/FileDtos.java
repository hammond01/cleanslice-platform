package dev.cleanslice.platform.files.infrastructure.rest;

import java.time.Instant;

/**
 * DTOs for file service responses.
 */
public class FileDtos {

    public record FileResponse(
            String fileId,
            String filename,
            long size,
            String contentType,
            Instant createdAt
    ) {}

    public record FileVersionResponse(
            String versionId,
            String fileId,
            int versionNumber,
            String filename,
            long size,
            String contentType,
            Instant createdAt,
            String createdBy
    ) {}

    public record UploadResponse(
            String fileId,
            String filename,
            long size,
            String contentType
    ) {}

    public record RestoreResponse(
            String fileId,
            String filename,
            int currentVersion,
            long size,
            String contentType
    ) {}
}