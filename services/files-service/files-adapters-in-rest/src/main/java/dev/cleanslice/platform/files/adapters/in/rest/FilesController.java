package dev.cleanslice.platform.files.adapters.in.rest;

import dev.cleanslice.platform.files.application.GetDownloadUrlUseCase;
import dev.cleanslice.platform.files.application.UploadFileUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
@Tag(name = "Files", description = "File management API")
public class FilesController {

    private final UploadFileUseCase uploadFileUseCase;
    private final GetDownloadUrlUseCase getDownloadUrlUseCase;

    public FilesController(UploadFileUseCase uploadFileUseCase, GetDownloadUrlUseCase getDownloadUrlUseCase) {
        this.uploadFileUseCase = uploadFileUseCase;
        this.getDownloadUrlUseCase = getDownloadUrlUseCase;
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
                    "versionId", UUID.randomUUID().toString()
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}/download")
    @Operation(summary = "Get download URL for a file")
    public ResponseEntity<Void> getDownloadUrl(@PathVariable UUID id) {
        var presignedUrl = getDownloadUrlUseCase.execute(id);

        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, presignedUrl)
                .build();
    }
}