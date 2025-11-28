package dev.cleanslice.platform.files.application.port;

import dev.cleanslice.platform.files.domain.FileVersion;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port for file version repository operations.
 */
public interface FileVersionRepositoryPort {

    FileVersion save(FileVersion fileVersion);

    Optional<FileVersion> findById(UUID id);

    List<FileVersion> findByFileId(UUID fileId);

    Integer findMaxVersionNumberByFileId(UUID fileId);
}