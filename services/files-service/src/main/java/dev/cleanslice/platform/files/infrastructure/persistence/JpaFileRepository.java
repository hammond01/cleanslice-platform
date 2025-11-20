package dev.cleanslice.platform.files.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Spring Data JPA repository for FileEntryEntity.
 */
@Repository
public interface JpaFileRepository extends JpaRepository<FileEntryEntity, UUID> {
	java.util.List<FileEntryEntity> findByOwnerId(UUID ownerId);
	java.util.List<FileEntryEntity> findByNameContainingIgnoreCaseOrContentTypeContainingIgnoreCase(String name, String contentType);
}
