package dev.cleanslice.platform.files.application.usecase;

import dev.cleanslice.platform.files.application.port.FileVersionRepositoryPort;
import dev.cleanslice.platform.files.application.port.StoragePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Use case for getting download URL for a specific file version.
 */
@Service
@RequiredArgsConstructor
public class GetFileVersionDownloadUrlUseCase {

    private final FileVersionRepositoryPort fileVersionRepository;
    private final StoragePort storagePort;

    public String execute(UUID versionId) {
        var version = fileVersionRepository.findById(versionId)
                .orElseThrow(() -> new IllegalArgumentException("File version not found: " + versionId));

        return storagePort.generatePresignedDownloadUrl(version.getStorageKey());
    }
}