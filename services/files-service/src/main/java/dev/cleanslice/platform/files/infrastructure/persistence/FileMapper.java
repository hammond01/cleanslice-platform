package dev.cleanslice.platform.files.infrastructure.persistence;

import dev.cleanslice.platform.files.domain.FileEntry;
import org.springframework.stereotype.Component;

/**
 * Mapper between domain model and JPA entity.
 */
@Component
public class FileMapper {

    public FileEntryEntity toEntity(FileEntry domain) {
        var entity = new FileEntryEntity();
        entity.setId(domain.getId());
        entity.setOwnerId(domain.getOwnerId());
        entity.setName(domain.getName());
        entity.setContentType(domain.getContentType());
        entity.setSize(domain.getSize());
        entity.setCurrentVersion(domain.getCurrentVersion());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        entity.setDeleted(domain.isDeleted());
        return entity;
    }

    public FileEntry toDomain(FileEntryEntity entity) {
        return new FileEntry(
                entity.getId(),
                entity.getOwnerId(),
                entity.getName(),
                entity.getContentType(),
                entity.getSize(),
                entity.getCurrentVersion(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.isDeleted()
        );
    }
}
