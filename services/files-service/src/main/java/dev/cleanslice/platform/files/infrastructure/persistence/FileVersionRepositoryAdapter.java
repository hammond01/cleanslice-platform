package dev.cleanslice.platform.files.infrastructure.persistence;

import dev.cleanslice.platform.files.application.port.FileVersionRepositoryPort;
import dev.cleanslice.platform.files.domain.FileVersion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter implementing FileVersionRepositoryPort using JPA.
 */
@Component
@RequiredArgsConstructor
public class FileVersionRepositoryAdapter implements FileVersionRepositoryPort {

    private final JpaFileVersionRepository jpaRepository;
    private final FileVersionMapper mapper;

    @Override
    public FileVersion save(FileVersion fileVersion) {
        var entity = mapper.toEntity(fileVersion);
        var savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<FileVersion> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<FileVersion> findByFileId(UUID fileId) {
        return jpaRepository.findByFileIdOrderByVersionNumberDesc(fileId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Integer findMaxVersionNumberByFileId(UUID fileId) {
        return jpaRepository.findMaxVersionNumberByFileId(fileId);
    }
}