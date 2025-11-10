# ADR 001: Use Presigned URLs for File Downloads

## Status
Accepted

## Context
The Files service needs to provide secure, direct access to stored files without streaming through the application server. Users should be able to download files efficiently with proper access control.

## Decision
Use S3/MinIO presigned URLs for file downloads instead of proxying through the application.

## Consequences
- **Positive**: Reduced server load, direct browser downloads, better performance
- **Positive**: Built-in expiration and access control via presigned URLs
- **Negative**: Requires S3-compatible storage (MinIO in dev)
- **Risk**: URL expiration needs proper TTL management

## Implementation
- Files service generates presigned URLs with configurable TTL
- Download endpoint returns 302 redirect to presigned URL
- Client handles direct download from storage