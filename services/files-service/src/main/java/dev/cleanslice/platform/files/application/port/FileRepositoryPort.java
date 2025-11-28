package dev.cleanslice.platform.files.application.port;

import dev.cleanslice.platform.files.domain.FileEntry;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Output port for file entry persistence.
 */
public interface FileRepositoryPort {
    FileEntry save(FileEntry fileEntry);
    Optional<FileEntry> findById(UUID id);
    Optional<FileEntry> findByOwnerIdAndName(UUID ownerId, String name);
    List<FileEntry> findByOwnerId(UUID ownerId);
    List<FileEntry> search(String name, String tag, String type);
    void delete(UUID id);
}
