package dev.cleanslice.platform.files.application;

import dev.cleanslice.platform.files.domain.FileEntryRepositoryPort;
import dev.cleanslice.platform.files.domain.FileStoragePort;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GetDownloadUrlUseCase {

    private final FileStoragePort fileStoragePort;
    private final FileEntryRepositoryPort fileEntryRepositoryPort;

    public GetDownloadUrlUseCase(FileStoragePort fileStoragePort, FileEntryRepositoryPort fileEntryRepositoryPort) {
        this.fileStoragePort = fileStoragePort;
        this.fileEntryRepositoryPort = fileEntryRepositoryPort;
    }

    public String execute(UUID fileId) {
        var fileEntry = fileEntryRepositoryPort.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("File not found"));

        if (fileEntry.isDeleted()) {
            throw new IllegalArgumentException("File is deleted");
        }

        // Generate presigned URL with 1 hour TTL
        return fileStoragePort.getPresignedReadUrl(fileId, 3600);
    }
}