package dev.cleanslice.platform.files.application.usecase;

import dev.cleanslice.platform.files.application.port.FileRepositoryPort;
import dev.cleanslice.platform.files.application.port.StoragePort;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Use case for generating presigned download URLs.
 */
@Service
public class GetDownloadUrlUseCase {

    private final StoragePort storagePort;
    private final FileRepositoryPort fileRepositoryPort;

    public GetDownloadUrlUseCase(StoragePort storagePort, FileRepositoryPort fileRepositoryPort) {
        this.storagePort = storagePort;
        this.fileRepositoryPort = fileRepositoryPort;
    }

    public String execute(UUID fileId) {
        var fileEntry = fileRepositoryPort.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("File not found"));

        if (fileEntry.isDeleted()) {
            throw new IllegalArgumentException("File is deleted");
        }

        // Generate presigned URL with 1 hour TTL
        return storagePort.getPresignedReadUrl(fileId, 3600);
    }
}
