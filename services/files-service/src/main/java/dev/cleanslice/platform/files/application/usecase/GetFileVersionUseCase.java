package dev.cleanslice.platform.files.application.usecase;

import dev.cleanslice.platform.files.application.port.FileVersionRepositoryPort;
import dev.cleanslice.platform.files.domain.FileVersion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Use case for retrieving metadata of a specific file version.
 */
@Service
@RequiredArgsConstructor
public class GetFileVersionUseCase {

    private final FileVersionRepositoryPort fileVersionRepository;

    public FileVersion execute(UUID versionId) {
        return fileVersionRepository.findById(versionId)
                .orElseThrow(() -> new IllegalArgumentException("File version not found: " + versionId));
    }
}
