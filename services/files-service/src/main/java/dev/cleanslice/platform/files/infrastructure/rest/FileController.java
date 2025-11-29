package dev.cleanslice.platform.files.infrastructure.rest;

import dev.cleanslice.platform.files.application.usecase.DeleteFileUseCase;
import dev.cleanslice.platform.files.application.usecase.GetDownloadUrlUseCase;
import dev.cleanslice.platform.files.application.usecase.GetFileVersionDownloadUrlUseCase;
import dev.cleanslice.platform.files.application.usecase.GetFileVersionUseCase;
import dev.cleanslice.platform.files.application.usecase.GetFileVersionsUseCase;
import dev.cleanslice.platform.files.application.usecase.RestoreFileVersionUseCase;
import dev.cleanslice.platform.files.application.usecase.UploadFileUseCase;
import dev.cleanslice.platform.files.application.port.FileRepositoryPort;
import dev.cleanslice.platform.files.infrastructure.config.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for file operations.
 */
@RestController
@RequestMapping("/api/files")
@Tag(name = "Files", description = "File management API")
@Slf4j
public class FileController {

    private final UploadFileUseCase uploadFileUseCase;
    private final GetDownloadUrlUseCase getDownloadUrlUseCase;
    private final GetFileVersionsUseCase getFileVersionsUseCase;
    private final GetFileVersionDownloadUrlUseCase getFileVersionDownloadUrlUseCase;
    private final GetFileVersionUseCase getFileVersionUseCase;
    private final RestoreFileVersionUseCase restoreFileVersionUseCase;
    private final DeleteFileUseCase deleteFileUseCase;
    private final FileRepositoryPort fileRepositoryPort;

    public FileController(UploadFileUseCase uploadFileUseCase,
                         GetDownloadUrlUseCase getDownloadUrlUseCase,
                         GetFileVersionsUseCase getFileVersionsUseCase,
                         GetFileVersionDownloadUrlUseCase getFileVersionDownloadUrlUseCase,
                         RestoreFileVersionUseCase restoreFileVersionUseCase,
                         DeleteFileUseCase deleteFileUseCase,
                         FileRepositoryPort fileRepositoryPort,
                         GetFileVersionUseCase getFileVersionUseCase) {
        this.uploadFileUseCase = uploadFileUseCase;
        this.getDownloadUrlUseCase = getDownloadUrlUseCase;
        this.getFileVersionsUseCase = getFileVersionsUseCase;
        this.getFileVersionDownloadUrlUseCase = getFileVersionDownloadUrlUseCase;
        this.getFileVersionUseCase = getFileVersionUseCase;
        this.restoreFileVersionUseCase = restoreFileVersionUseCase;
        this.deleteFileUseCase = deleteFileUseCase;
        this.fileRepositoryPort = fileRepositoryPort;
    }

    @GetMapping("/version/{versionId}")
    @Operation(summary = "Get metadata for a specific file version")
    public ResponseEntity<Map<String, Object>> getFileVersion(@PathVariable UUID versionId) {
        try {
            var version = getFileVersionUseCase.execute(versionId);
            Map<String, Object> response = Map.of(
                    "versionId", version.getId().toString(),
                    "fileId", version.getFileId().toString(),
                    "versionNumber", version.getVersionNumber(),
                    "filename", version.getName(),
                    "size", version.getSize(),
                    "contentType", version.getContentType(),
                    "createdAt", version.getCreatedAt().toString(),
                    "createdBy", version.getCreatedBy().toString()
            );
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @Operation(summary = "Upload a file")
    public ResponseEntity<Map<String, Object>> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            // Get user ID from JWT token via SecurityContext
            UUID userId;
            try {
                userId = SecurityUtils.getCurrentUserId();
            } catch (IllegalStateException e) {
                // Development mode: use a default user ID
                log.warn("No authenticated user found, using default user ID");
                userId = UUID.fromString("00000000-0000-0000-0000-000000000001");
            }

            var fileEntry = uploadFileUseCase.execute(
                    userId,
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getSize(),
                    file.getInputStream()
            );

            Map<String, Object> response = Map.of(
                    "fileId", fileEntry.getId().toString(),
                    "filename", fileEntry.getName(),
                    "size", fileEntry.getSize(),
                    "contentType", fileEntry.getContentType()
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error uploading file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to upload file: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}/download")
    @Operation(summary = "Get download URL for a file")
    public ResponseEntity<Void> getDownloadUrl(@PathVariable UUID id) {
        try {
            var presignedUrl = getDownloadUrlUseCase.execute(id);
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, presignedUrl)
                    .build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a file (soft delete)")
    public ResponseEntity<Void> deleteFile(@PathVariable UUID id) {
        try {
            deleteFileUseCase.execute(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/owner/{ownerId}")
    @Operation(summary = "Get all files by owner")
    public ResponseEntity<List<Map<String, Object>>> getFilesByOwner(@PathVariable UUID ownerId) {
        var files = fileRepositoryPort.findByOwnerId(ownerId);
        var results = files.stream().map(file -> Map.<String, Object>of(
                "fileId", file.getId().toString(),
                "filename", file.getName(),
                "size", file.getSize(),
                "contentType", file.getContentType(),
                "createdAt", file.getCreatedAt().toString()
        )).toList();
        return ResponseEntity.ok(results);
    }

    @GetMapping("/search")
    @Operation(summary = "Search files by name or type")
    public ResponseEntity<List<Map<String, Object>>> searchFiles(@RequestParam(required = false) String name,
                                                                 @RequestParam(required = false) String type) {
        var files = fileRepositoryPort.search(name, null, type);
        var results = files.stream().map(file -> Map.<String, Object>of(
                "fileId", file.getId().toString(),
                "filename", file.getName(),
                "size", file.getSize(),
                "contentType", file.getContentType(),
                "createdAt", file.getCreatedAt().toString()
        )).toList();
        return ResponseEntity.ok(results);
    }

    @GetMapping("/{id}/versions")
    @Operation(summary = "Get all versions of a file")
    public ResponseEntity<List<Map<String, Object>>> getFileVersions(@PathVariable UUID id) {
        var versions = getFileVersionsUseCase.execute(id);
        var results = versions.stream().map(version -> Map.<String, Object>of(
                "versionId", version.getId().toString(),
                "versionNumber", version.getVersionNumber(),
                "filename", version.getName(),
                "size", version.getSize(),
                "contentType", version.getContentType(),
                "createdAt", version.getCreatedAt().toString(),
                "createdBy", version.getCreatedBy().toString()
        )).toList();
        return ResponseEntity.ok(results);
    }

    @GetMapping("/version/{versionId}/download")
    @Operation(summary = "Get download URL for a specific file version")
    public ResponseEntity<Void> getFileVersionDownloadUrl(@PathVariable UUID versionId) {
        try {
            var presignedUrl = getFileVersionDownloadUrlUseCase.execute(versionId);
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, presignedUrl)
                    .build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/restore/{versionNumber}")
    @Operation(summary = "Restore file to a specific version")
    public ResponseEntity<Map<String, Object>> restoreFileVersion(@PathVariable UUID id, @PathVariable int versionNumber) {
        try {
            var restoredFile = restoreFileVersionUseCase.execute(id, versionNumber);
            Map<String, Object> response = Map.of(
                    "fileId", restoredFile.getId().toString(),
                    "filename", restoredFile.getName(),
                    "currentVersion", restoredFile.getCurrentVersion(),
                    "size", restoredFile.getSize(),
                    "contentType", restoredFile.getContentType()
            );
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
