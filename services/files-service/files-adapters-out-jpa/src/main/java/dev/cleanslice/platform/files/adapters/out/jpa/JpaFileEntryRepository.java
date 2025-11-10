package dev.cleanslice.platform.files.adapters.out.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface JpaFileEntryRepository extends JpaRepository<FileEntryEntity, UUID> {
}
