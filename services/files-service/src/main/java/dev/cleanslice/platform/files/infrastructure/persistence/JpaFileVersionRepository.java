package dev.cleanslice.platform.files.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * JPA repository for file versions.
 */
@Repository
public interface JpaFileVersionRepository extends JpaRepository<FileVersionEntity, UUID> {

    List<FileVersionEntity> findByFileIdOrderByVersionNumberDesc(UUID fileId);

    @Query("SELECT MAX(v.versionNumber) FROM FileVersionEntity v WHERE v.fileId = :fileId")
    Integer findMaxVersionNumberByFileId(@Param("fileId") UUID fileId);
}