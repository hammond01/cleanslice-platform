package dev.cleanslice.platform.files.infrastructure.rest;

import dev.cleanslice.platform.common.api.ApiResponse;
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
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

/**
 * Reactive REST controller for file operations.
 * Provides non-blocking endpoints using Spring WebFlux.
 */
@RestController
@RequestMapping("/api/v2/files")
@Tag(name = "Files (Reactive)", description = "Reactive file management API")
@Slf4j
@RequiredArgsConstructor
public class ReactiveFileController {

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

    @GetMapping("/version/{versionId}")
    @Operation(summary = "Get metadata for a specific file version (Reactive)")
    public Mono<ResponseEntity<ApiResponse<FileDtos.FileVersionResponse>>> getFileVersion(@PathVariable UUID versionId) {
        return Mono.fromCallable(() -> {
            try {
                var version = getFileVersionUseCase.execute(versionId);
                return fileMapper.toFileVersionResponse(version);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("File version not found: " + versionId);
            }
        })
        .map(response -> ResponseEntity.ok(ApiResponse.success(response)))
        .onErrorResume(IllegalArgumentException.class, e ->
            Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("File version not found: " + versionId)))
        );
    }

    @PostMapping
    @Operation(summary = "Upload a file (Reactive)")
    public Mono<ResponseEntity<ApiResponse<FileDtos.UploadResponse>>> uploadFile(@RequestParam("file") MultipartFile file) {
        return Mono.fromCallable(() -> {
            var userId = getCurrentUserId();
            var fileEntry = uploadFileUseCase.execute(
                    userId,
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getSize(),
                    file.getInputStream()
            );
            return fileMapper.toUploadResponse(fileEntry);
        })
        .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response)))
        .onErrorResume(Exception.class, e -> {
            log.error("Error uploading file", e);
            return Mono.<ResponseEntity<ApiResponse<FileDtos.UploadResponse>>>just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<FileDtos.UploadResponse>error("Failed to upload file: " + e.getMessage())));
        });
    }

    @GetMapping("/{id}/download")
    @Operation(summary = "Get download URL for a file (Reactive)")
    public Mono<ResponseEntity<ApiResponse<String>>> getDownloadUrl(@PathVariable UUID id) {
        return Mono.fromCallable(() -> {
            try {
                var presignedUrl = getDownloadUrlUseCase.execute(id);
                return ResponseEntity.status(HttpStatus.FOUND)
                        .header(HttpHeaders.LOCATION, presignedUrl)
                        .body(ApiResponse.success("Redirecting to download URL"));
            } catch (IllegalArgumentException e) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.<String>error("File not found: " + id));
            }
        });
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a file (soft delete) (Reactive)")
    public Mono<ResponseEntity<ApiResponse<Void>>> deleteFile(@PathVariable UUID id) {
        return Mono.fromCallable(() -> {
            deleteFileUseCase.execute(id);
            return null;
        })
        .then(Mono.just(ResponseEntity.ok(ApiResponse.success((Void) null, "File deleted successfully"))))
        .onErrorResume(IllegalArgumentException.class, e ->
            Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("File not found: " + id)))
        );
    }

    @GetMapping("/owner/{ownerId}")
    @Operation(summary = "Get all files by owner (Reactive)")
    public Mono<ResponseEntity<ApiResponse<List<FileDtos.FileResponse>>>> getFilesByOwner(@PathVariable UUID ownerId) {
        return Mono.fromCallable(() -> {
            var files = fileRepositoryPort.findByOwnerId(ownerId);
            return files.stream().map(fileMapper::toFileResponse).toList();
        })
        .map(results -> ResponseEntity.ok(ApiResponse.success(results)));
    }

    @GetMapping("/search")
    @Operation(summary = "Search files by name or type (Reactive)")
    public Mono<ResponseEntity<ApiResponse<List<FileDtos.FileResponse>>>> searchFiles(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String type) {
        return Mono.fromCallable(() -> {
            var files = fileRepositoryPort.search(name, null, type);
            return files.stream().map(fileMapper::toFileResponse).toList();
        })
        .map(results -> ResponseEntity.ok(ApiResponse.success(results)));
    }

    @GetMapping("/{id}/versions")
    @Operation(summary = "Get all versions of a file (Reactive)")
    public Mono<ResponseEntity<ApiResponse<List<FileDtos.FileVersionResponse>>>> getFileVersions(@PathVariable UUID id) {
        return Mono.fromCallable(() -> {
            var versions = getFileVersionsUseCase.execute(id);
            return versions.stream().map(fileMapper::toFileVersionResponse).toList();
        })
        .map(results -> ResponseEntity.ok(ApiResponse.success(results)));
    }

    @GetMapping("/version/{versionId}/download")
    @Operation(summary = "Get download URL for a specific file version (Reactive)")
    public Mono<ResponseEntity<ApiResponse<String>>> getFileVersionDownloadUrl(@PathVariable UUID versionId) {
        return Mono.fromCallable(() -> {
            try {
                var presignedUrl = getFileVersionDownloadUrlUseCase.execute(versionId);
                return ResponseEntity.status(HttpStatus.FOUND)
                        .header(HttpHeaders.LOCATION, presignedUrl)
                        .body(ApiResponse.success("Redirecting to download URL"));
            } catch (IllegalArgumentException e) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.<String>error("File version not found: " + versionId));
            }
        });
    }

    @PostMapping("/{id}/restore/{versionNumber}")
    @Operation(summary = "Restore file to a specific version (Reactive)")
    public Mono<ResponseEntity<ApiResponse<FileDtos.RestoreResponse>>> restoreFileVersion(
            @PathVariable UUID id,
            @PathVariable int versionNumber) {
        return Mono.fromCallable(() -> {
            var restoredFile = restoreFileVersionUseCase.execute(id, versionNumber);
            return fileMapper.toRestoreResponse(restoredFile);
        })
        .map(response -> ResponseEntity.ok(ApiResponse.success(response)))
        .onErrorResume(IllegalArgumentException.class, e ->
            Mono.<ResponseEntity<ApiResponse<FileDtos.RestoreResponse>>>just(ResponseEntity.badRequest()
                    .body(ApiResponse.<FileDtos.RestoreResponse>error("Invalid version number or file not found")))
        );
    }
}