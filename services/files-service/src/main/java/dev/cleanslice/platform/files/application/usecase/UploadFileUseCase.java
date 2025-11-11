package dev.cleanslice.platform.files.application.usecase;

import dev.cleanslice.platform.files.application.port.FileEventPublisherPort;
import dev.cleanslice.platform.files.application.port.FileRepositoryPort;
import dev.cleanslice.platform.files.application.port.StoragePort;
import dev.cleanslice.platform.files.domain.FileEntry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.UUID;

/**
 * Use case for uploading files.
 */
@Service
public class UploadFileUseCase {

    private final StoragePort storagePort;
    private final FileRepositoryPort fileRepositoryPort;
    private final FileEventPublisherPort eventPublisherPort;

    public UploadFileUseCase(StoragePort storagePort,
                            FileRepositoryPort fileRepositoryPort,
                            FileEventPublisherPort eventPublisherPort) {
        this.storagePort = storagePort;
        this.fileRepositoryPort = fileRepositoryPort;
        this.eventPublisherPort = eventPublisherPort;
    }

    @Transactional
    public FileEntry execute(UUID ownerId, String filename, String contentType, long size, InputStream content) {
        // Create domain object
        var fileEntry = FileEntry.create(ownerId, filename, contentType, size);
        
        // Store file in S3/MinIO
        storagePort.put(fileEntry.getId(), content, size, contentType);

        // Save metadata to database
        var savedFile = fileRepositoryPort.save(fileEntry);
        
        // Publish event
        eventPublisherPort.publishFileUploaded(savedFile);
        
        return savedFile;
    }
}
