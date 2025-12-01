package dev.cleanslice.platform.files.infrastructure.rest;

import dev.cleanslice.platform.files.domain.FileEntry;
import dev.cleanslice.platform.files.domain.FileVersion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting domain objects to DTOs.
 */
@Mapper(componentModel = "spring")
public interface FileMapper {

    @Mapping(target = "fileId", expression = "java(fileEntry.getId().toString())")
    @Mapping(target = "filename", source = "name")
    FileDtos.FileResponse toFileResponse(FileEntry fileEntry);

    @Mapping(target = "versionId", expression = "java(fileVersion.getId().toString())")
    @Mapping(target = "fileId", expression = "java(fileVersion.getFileId().toString())")
    @Mapping(target = "filename", source = "name")
    @Mapping(target = "createdBy", expression = "java(fileVersion.getCreatedBy().toString())")
    FileDtos.FileVersionResponse toFileVersionResponse(FileVersion fileVersion);

    @Mapping(target = "fileId", expression = "java(fileEntry.getId().toString())")
    @Mapping(target = "filename", source = "name")
    FileDtos.UploadResponse toUploadResponse(FileEntry fileEntry);

    @Mapping(target = "fileId", expression = "java(fileEntry.getId().toString())")
    @Mapping(target = "filename", source = "name")
    FileDtos.RestoreResponse toRestoreResponse(FileEntry fileEntry);
}