package dev.cleanslice.platform.files.application.usecase;

import dev.cleanslice.platform.files.application.port.FileEventPublisherPort;
import dev.cleanslice.platform.files.application.port.FileRepositoryPort;
import dev.cleanslice.platform.files.application.port.FileVersionRepositoryPort;
import dev.cleanslice.platform.files.application.port.StoragePort;
import dev.cleanslice.platform.files.domain.FileEntry;
import dev.cleanslice.platform.files.domain.FileVersion;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.UUID;

/**
 * Use case for uploading files.
 * Supports versioning - if file with same name exists, creates new version.
 */
@Service
public class UploadFileUseCase {

    private final StoragePort storagePort;
    private final FileRepositoryPort fileRepositoryPort;
    private final FileVersionRepositoryPort fileVersionRepositoryPort;
    private final FileEventPublisherPort eventPublisherPort;

    public UploadFileUseCase(StoragePort storagePort,
                            FileRepositoryPort fileRepositoryPort,
                            FileVersionRepositoryPort fileVersionRepositoryPort,
                            FileEventPublisherPort eventPublisherPort) {
        this.storagePort = storagePort;
        this.fileRepositoryPort = fileRepositoryPort;
        this.fileVersionRepositoryPort = fileVersionRepositoryPort;
        this.eventPublisherPort = eventPublisherPort;
    }

    @Transactional
    public FileEntry execute(UUID ownerId, String filename, String contentType, long size, InputStream content) {
        // Check if file with same name already exists
        var existingFile = fileRepositoryPort.findByOwnerIdAndName(ownerId, filename);

        FileEntry savedFile;
        String storageKey;

        if (existingFile.isPresent()) {
            // Create new version
            var file = existingFile.get();
            int nextVersion = (fileVersionRepositoryPort.findMaxVersionNumberByFileId(file.getId()) != null)
                    ? fileVersionRepositoryPort.findMaxVersionNumberByFileId(file.getId()) + 1 : 2;

            storageKey = file.getId() + "/v" + nextVersion;

            // Store file in S3/MinIO with versioned key
            storagePort.put(storageKey, content, size, contentType);

            // Create version record
            var fileVersion = FileVersion.create(file.getId(), nextVersion, filename, contentType, size, storageKey, ownerId);
            fileVersionRepositoryPort.save(fileVersion);

            // Publish version uploaded event
            eventPublisherPort.publishFileVersionUploaded(fileVersion);

            // Update file entry
            savedFile = fileRepositoryPort.save(file.updateToNewVersion(filename, contentType, size, nextVersion));

        } else {
            // Create new file
            var fileEntry = FileEntry.create(ownerId, filename, contentType, size);
            storageKey = fileEntry.getId().toString();

            // Store file in S3/MinIO
            storagePort.put(storageKey, content, size, contentType);

            // Save metadata to database
            savedFile = fileRepositoryPort.save(fileEntry);

            // Create initial version record
            var fileVersion = FileVersion.create(savedFile.getId(), 1, filename, contentType, size, storageKey, ownerId);
            fileVersionRepositoryPort.save(fileVersion);

            // Publish version uploaded event
            eventPublisherPort.publishFileVersionUploaded(fileVersion);
        }

        // Publish event
        eventPublisherPort.publishFileUploaded(savedFile);

        return savedFile;
    }
}
