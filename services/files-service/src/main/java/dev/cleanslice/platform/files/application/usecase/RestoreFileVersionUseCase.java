package dev.cleanslice.platform.files.application.usecase;

import dev.cleanslice.platform.files.application.port.FileRepositoryPort;
import dev.cleanslice.platform.files.application.port.FileVersionRepositoryPort;
import dev.cleanslice.platform.files.domain.FileEntry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Use case for restoring a file to a specific version.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RestoreFileVersionUseCase {

    private final FileRepositoryPort fileRepository;
    private final FileVersionRepositoryPort fileVersionRepository;

    @Transactional
    public FileEntry execute(UUID fileId, int versionNumber) {
        var file = fileRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("File not found: " + fileId));

        var versions = fileVersionRepository.findByFileId(fileId);
        var targetVersion = versions.stream()
                .filter(v -> v.getVersionNumber() == versionNumber)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Version not found: " + versionNumber));

        // Update file entry to point to the restored version
        var updatedFile = file.updateToNewVersion(
                targetVersion.getName(),
                targetVersion.getContentType(),
                targetVersion.getSize(),
                versionNumber
        );

                var saved = fileRepository.save(updatedFile);
                log.info("Restored file {} to version {}", fileId, versionNumber);
                return saved;
    }
}