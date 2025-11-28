package dev.cleanslice.platform.files.application.usecase;

import dev.cleanslice.platform.files.application.port.FileVersionRepositoryPort;
import dev.cleanslice.platform.files.domain.FileVersion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Use case for retrieving file versions.
 */
@Service
@RequiredArgsConstructor
public class GetFileVersionsUseCase {

    private final FileVersionRepositoryPort fileVersionRepository;

    public List<FileVersion> execute(UUID fileId) {
        return fileVersionRepository.findByFileId(fileId);
    }
}