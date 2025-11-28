-- Create databases for all services
CREATE DATABASE productdb;
CREATE DATABASE filesdb;
CREATE DATABASE auditdb;
CREATE DATABASE identitydb;

-- Switch to filesdb and create schema
\c filesdb;

-- Create file_entries table with versioning support
CREATE TABLE file_entries (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    size BIGINT NOT NULL,
    current_version INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- Create file_versions table
CREATE TABLE file_versions (
    id UUID PRIMARY KEY,
    file_id UUID NOT NULL REFERENCES file_entries(id),
    version_number INT NOT NULL,
    name VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    size BIGINT NOT NULL,
    storage_key VARCHAR(500) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    created_by UUID NOT NULL,
    UNIQUE(file_id, version_number)
);

-- Create indexes
CREATE INDEX idx_file_entries_owner_id ON file_entries(owner_id);
CREATE INDEX idx_file_entries_owner_name ON file_entries(owner_id, name);
CREATE INDEX idx_file_versions_file_id ON file_versions(file_id);
