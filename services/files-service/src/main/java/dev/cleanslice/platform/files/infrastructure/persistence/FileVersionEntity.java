package dev.cleanslice.platform.files.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity for file versions.
 */
@Entity
@Table(name = "file_versions")
@Getter
@Setter
public class FileVersionEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID fileId;

    @Column(nullable = false)
    private int versionNumber;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, length = 100)
    private String contentType;

    @Column(nullable = false)
    private long size;

    @Column(nullable = false, length = 500)
    private String storageKey;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private UUID createdBy;
}