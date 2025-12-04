package dev.cleanslice.platform.files.infrastructure.storage;

import dev.cleanslice.platform.files.application.port.StoragePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.InputStream;
import java.net.URI;
import java.time.Duration;

/**
 * Adapter for S3/MinIO storage operations.
 */
@Component
public class S3StorageAdapter implements StoragePort {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final String bucketName;

    public S3StorageAdapter(
            @Value("${storage.s3.endpoint}") String endpoint,
            @Value("${storage.s3.access-key}") String accessKey,
            @Value("${storage.s3.secret-key}") String secretKey,
            @Value("${storage.s3.bucket}") String bucketName,
            @Value("${storage.s3.region}") String region) {

        this.bucketName = bucketName;

        var credentials = AwsBasicCredentials.create(accessKey, secretKey);
        var credentialsProvider = StaticCredentialsProvider.create(credentials);

        this.s3Client = S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider)
                .forcePathStyle(true)
                .build();

        this.s3Presigner = S3Presigner.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider)
                .build();

        // Create bucket if not exists
        createBucketIfNotExists();
    }

    private void createBucketIfNotExists() {
        try {
            s3Client.headBucket(b -> b.bucket(bucketName));
        } catch (Exception e) {
            s3Client.createBucket(b -> b.bucket(bucketName));
        }
    }

    @Override
    public void put(String storageKey, InputStream content, long size, String contentType) {
        var putRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(storageKey)
                .contentType(contentType)
                .contentLength(size)
                .build();

        s3Client.putObject(putRequest, RequestBody.fromInputStream(content, size));
    }

    @Override
    public InputStream get(String storageKey) {
        var getRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(storageKey)
                .build();

        return s3Client.getObject(getRequest);
    }

    @Override
    public void delete(String storageKey) {
        s3Client.deleteObject(b -> b.bucket(bucketName).key(storageKey));
    }

    @Override
    public String generatePresignedDownloadUrl(String storageKey) {
        var getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(storageKey)
                .build();

        var presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(3600)) // 1 hour
                .getObjectRequest(getObjectRequest)
                .build();

        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }

    @Override
    public boolean exists(String storageKey) {
        try {
            var headRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(storageKey)
                    .build();
            s3Client.headObject(headRequest);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
