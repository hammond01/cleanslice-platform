package dev.cleanslice.platform.files.infrastructure.persistence;

import dev.cleanslice.platform.files.application.port.FileRepositoryPort;
import dev.cleanslice.platform.files.domain.FileEntry;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter implementing FileRepositoryPort using JPA.
 */
@Component
public class FileRepositoryAdapter implements FileRepositoryPort {

    private final JpaFileRepository jpaRepository;
    private final FileMapper mapper;

    public FileRepositoryAdapter(JpaFileRepository jpaRepository, FileMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public FileEntry save(FileEntry fileEntry) {
        var entity = mapper.toEntity(fileEntry);
        var saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<FileEntry> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<FileEntry> findByOwnerIdAndName(UUID ownerId, String name) {
        return jpaRepository.findByOwnerIdAndName(ownerId, name).map(mapper::toDomain);
    }

    @Override
    public List<FileEntry> findByOwnerId(UUID ownerId) {
        return jpaRepository.findByOwnerId(ownerId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<FileEntry> search(String name, String tag, String type) {
        // Basic search on name and contentType (type maps to contentType here)
        String searchName = (name == null) ? "" : name;
        String searchContentType = (type == null) ? "" : type;
        return jpaRepository
                .findByNameContainingIgnoreCaseOrContentTypeContainingIgnoreCase(searchName, searchContentType)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public void delete(UUID id) {
        jpaRepository.deleteById(id);
    }
}
