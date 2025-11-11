package dev.cleanslice.platform.files.infrastructure.rest;

import dev.cleanslice.platform.files.application.usecase.DeleteFileUseCase;
import dev.cleanslice.platform.files.application.usecase.GetDownloadUrlUseCase;
import dev.cleanslice.platform.files.application.usecase.UploadFileUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    private final DeleteFileUseCase deleteFileUseCase;

    public FileController(UploadFileUseCase uploadFileUseCase,
                         GetDownloadUrlUseCase getDownloadUrlUseCase,
                         DeleteFileUseCase deleteFileUseCase) {
        this.uploadFileUseCase = uploadFileUseCase;
        this.getDownloadUrlUseCase = getDownloadUrlUseCase;
        this.deleteFileUseCase = deleteFileUseCase;
    }

    @PostMapping
    @Operation(summary = "Upload a file")
    public ResponseEntity<Map<String, Object>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestHeader("X-User-Id") UUID userId) {

        try {
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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
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
}
