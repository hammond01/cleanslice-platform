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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for file operations.
 */
@RestController
@RequestMapping("/api/files")
@Tag(name = "Files", description = "File management API")
@Slf4j
@RequiredArgsConstructor
public class FileController {

    private final UploadFileUseCase uploadFileUseCase;
    private final GetDownloadUrlUseCase getDownloadUrlUseCase;
    private final GetFileVersionsUseCase getFileVersionsUseCase;
    private final GetFileVersionDownloadUrlUseCase getFileVersionDownloadUrlUseCase;
    private final GetFileVersionUseCase getFileVersionUseCase;
    private final RestoreFileVersionUseCase restoreFileVersionUseCase;
    private final DeleteFileUseCase deleteFileUseCase;
    private final FileRepositoryPort fileRepositoryPort;
    private final FileMapper fileMapper;

    private static final UUID DEFAULT_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private UUID getCurrentUserId() {
        try {
            return SecurityUtils.getCurrentUserId();
        } catch (IllegalStateException e) {
            log.warn("No authenticated user found, using default user ID");
            return DEFAULT_USER_ID;
        }
    }

    private <T> ResponseEntity<T> handleNotFound(IllegalArgumentException e) {
        log.debug("Resource not found: {}", e.getMessage());
        return ResponseEntity.notFound().build();
    }

    private ResponseEntity<Void> handleDownloadUrl(String url) {
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, url)
                .build();
    }

    @GetMapping("/version/{versionId}")
    @Operation(summary = "Get metadata for a specific file version")
    public ResponseEntity<FileDtos.FileVersionResponse> getFileVersion(@PathVariable UUID versionId) {
        try {
            var version = getFileVersionUseCase.execute(versionId);
            return ResponseEntity.ok(fileMapper.toFileVersionResponse(version));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @Operation(summary = "Upload a file")
    public ResponseEntity<FileDtos.UploadResponse> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            var userId = getCurrentUserId();
            var fileEntry = uploadFileUseCase.execute(
                    userId,
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getSize(),
                    file.getInputStream()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(fileMapper.toUploadResponse(fileEntry));
        } catch (Exception e) {
            log.error("Error uploading file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}/download")
    @Operation(summary = "Get download URL for a file")
    public ResponseEntity<Void> getDownloadUrl(@PathVariable UUID id) {
        try {
            var presignedUrl = getDownloadUrlUseCase.execute(id);
            return handleDownloadUrl(presignedUrl);
        } catch (IllegalArgumentException e) {
            return handleNotFound(e);
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a file (soft delete)")
    public ResponseEntity<Void> deleteFile(@PathVariable UUID id) {
        try {
            deleteFileUseCase.execute(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return handleNotFound(e);
        }
    }

    @GetMapping("/owner/{ownerId}")
    @Operation(summary = "Get all files by owner")
    public ResponseEntity<List<FileDtos.FileResponse>> getFilesByOwner(@PathVariable UUID ownerId) {
        var files = fileRepositoryPort.findByOwnerId(ownerId);
        var results = files.stream().map(fileMapper::toFileResponse).toList();
        return ResponseEntity.ok(results);
    }

    @GetMapping("/search")
    @Operation(summary = "Search files by name or type")
    public ResponseEntity<List<FileDtos.FileResponse>> searchFiles(@RequestParam(required = false) String name,
                                                                 @RequestParam(required = false) String type) {
        var files = fileRepositoryPort.search(name, null, type);
        var results = files.stream().map(fileMapper::toFileResponse).toList();
        return ResponseEntity.ok(results);
    }

    @GetMapping("/{id}/versions")
    @Operation(summary = "Get all versions of a file")
    public ResponseEntity<List<FileDtos.FileVersionResponse>> getFileVersions(@PathVariable UUID id) {
        var versions = getFileVersionsUseCase.execute(id);
        var results = versions.stream().map(fileMapper::toFileVersionResponse).toList();
        return ResponseEntity.ok(results);
    }

    @GetMapping("/version/{versionId}/download")
    @Operation(summary = "Get download URL for a specific file version")
    public ResponseEntity<Void> getFileVersionDownloadUrl(@PathVariable UUID versionId) {
        try {
            var presignedUrl = getFileVersionDownloadUrlUseCase.execute(versionId);
            return handleDownloadUrl(presignedUrl);
        } catch (IllegalArgumentException e) {
            return handleNotFound(e);
        }
    }

    @PostMapping("/{id}/restore/{versionNumber}")
    @Operation(summary = "Restore file to a specific version")
    public ResponseEntity<FileDtos.RestoreResponse> restoreFileVersion(@PathVariable UUID id, @PathVariable int versionNumber) {
        try {
            var restoredFile = restoreFileVersionUseCase.execute(id, versionNumber);
            return ResponseEntity.ok(fileMapper.toRestoreResponse(restoredFile));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
