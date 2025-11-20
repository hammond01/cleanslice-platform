package dev.cleanslice.platform.files.application.usecase;

import dev.cleanslice.platform.files.application.port.FileEventPublisherPort;
import dev.cleanslice.platform.files.application.port.FileRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Use case for soft deleting files.
 */
@Service
public class DeleteFileUseCase {

    private final FileRepositoryPort fileRepositoryPort;
    private final FileEventPublisherPort eventPublisherPort;

    public DeleteFileUseCase(FileRepositoryPort fileRepositoryPort,
                            FileEventPublisherPort eventPublisherPort) {
        this.fileRepositoryPort = fileRepositoryPort;
        this.eventPublisherPort = eventPublisherPort;
    }

    @Transactional
    public void execute(UUID fileId) {
        var fileEntry = fileRepositoryPort.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("File not found"));

        // Soft delete in database
        var deletedFile = fileEntry.markDeleted();
        fileRepositoryPort.save(deletedFile);
        
        // Optionally delete from storage (hard delete)
        // storagePort.delete(fileId);
        
        // Publish event
        eventPublisherPort.publishFileDeleted(fileId);
    }
}
