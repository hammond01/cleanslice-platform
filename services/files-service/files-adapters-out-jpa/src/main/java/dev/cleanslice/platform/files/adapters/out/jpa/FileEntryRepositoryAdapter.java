package dev.cleanslice.platform.files.adapters.out.jpa;

import dev.cleanslice.platform.files.domain.FileEntry;
import dev.cleanslice.platform.files.domain.FileEntryRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@SuppressWarnings("null")
public class FileEntryRepositoryAdapter implements FileEntryRepositoryPort {

    private final JpaFileEntryRepository jpaRepository;

    public FileEntryRepositoryAdapter(JpaFileEntryRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public FileEntry save(FileEntry fileEntry) {
        var entity = toEntity(fileEntry);
        var saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<FileEntry> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<FileEntry> findByOwnerId(UUID ownerId) {
        // TODO: Implement when JpaRepository method is added
        return Collections.emptyList();
    }

    @Override
    public List<FileEntry> search(String name, String tag, String type) {
        // TODO: Implement search functionality
        return Collections.emptyList();
    }

    @Override
    public void delete(UUID id) {
        jpaRepository.deleteById(id);
    }

    private FileEntryEntity toEntity(FileEntry domain) {
        var entity = new FileEntryEntity();
        entity.setId(domain.getId());
        entity.setOwnerId(domain.getOwnerId());
        entity.setName(domain.getName());
        entity.setContentType(domain.getContentType());
        entity.setSize(domain.getSize());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        entity.setDeleted(domain.isDeleted());
        return entity;
    }

    private FileEntry toDomain(FileEntryEntity entity) {
        return new FileEntry(
                entity.getId(),
                entity.getOwnerId(),
                entity.getName(),
                entity.getContentType(),
                entity.getSize(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.isDeleted()
        );
    }
}
