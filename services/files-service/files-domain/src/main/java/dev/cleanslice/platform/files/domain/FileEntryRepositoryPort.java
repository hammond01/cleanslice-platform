package dev.cleanslice.platform.files.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FileEntryRepositoryPort {
    FileEntry save(FileEntry fileEntry);
    Optional<FileEntry> findById(UUID id);
    List<FileEntry> findByOwnerId(UUID ownerId);
    List<FileEntry> search(String name, String tag, String type);
    void delete(UUID id);
}