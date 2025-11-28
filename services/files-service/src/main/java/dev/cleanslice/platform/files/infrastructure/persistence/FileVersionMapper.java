package dev.cleanslice.platform.files.infrastructure.persistence;

import dev.cleanslice.platform.files.domain.FileVersion;
import org.springframework.stereotype.Component;

/**
 * Mapper between FileVersion domain model and JPA entity.
 */
@Component
public class FileVersionMapper {

    public FileVersionEntity toEntity(FileVersion domain) {
        var entity = new FileVersionEntity();
        entity.setId(domain.getId());
        entity.setFileId(domain.getFileId());
        entity.setVersionNumber(domain.getVersionNumber());
        entity.setName(domain.getName());
        entity.setContentType(domain.getContentType());
        entity.setSize(domain.getSize());
        entity.setStorageKey(domain.getStorageKey());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setCreatedBy(domain.getCreatedBy());
        return entity;
    }

    public FileVersion toDomain(FileVersionEntity entity) {
        return new FileVersion(
            entity.getId(),
            entity.getFileId(),
            entity.getVersionNumber(),
            entity.getName(),
            entity.getContentType(),
            entity.getSize(),
            entity.getStorageKey(),
            entity.getCreatedAt(),
            entity.getCreatedBy()
        );
    }
}