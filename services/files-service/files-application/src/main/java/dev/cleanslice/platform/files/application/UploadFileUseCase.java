package dev.cleanslice.platform.files.application;

import dev.cleanslice.platform.files.domain.FileEntry;
import dev.cleanslice.platform.files.domain.FileEntryRepositoryPort;
import dev.cleanslice.platform.files.domain.FileStoragePort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.UUID;

@Service
public class UploadFileUseCase {

    private final FileStoragePort fileStoragePort;
    private final FileEntryRepositoryPort fileEntryRepositoryPort;

    public UploadFileUseCase(FileStoragePort fileStoragePort, FileEntryRepositoryPort fileEntryRepositoryPort) {
        this.fileStoragePort = fileStoragePort;
        this.fileEntryRepositoryPort = fileEntryRepositoryPort;
    }

    @Transactional
    public FileEntry execute(UUID ownerId, String filename, String contentType, long size, InputStream content) {
        // Store file in storage
        var fileEntry = FileEntry.create(ownerId, filename, contentType, size);
        fileStoragePort.put(fileEntry.getId(), content, size, contentType);

        // Save metadata
        return fileEntryRepositoryPort.save(fileEntry);
    }
}