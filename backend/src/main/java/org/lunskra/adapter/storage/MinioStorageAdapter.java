package org.lunskra.adapter.storage;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.http.Method;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.lunskra.port.out.ImageStoragePort;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * {@link ImageStoragePort} implementation that stores member images in a MinIO
 * S3-compatible object store.
 * <p>
 * On startup ({@link jakarta.annotation.PostConstruct}) a {@link MinioClient} is
 * constructed from the configured endpoint and credentials, and the target bucket is
 * created if it does not yet exist. Each image is stored under a randomly generated
 * UUID key so that filenames from the client do not leak into storage paths.
 * <p>
 * Configuration properties (via MicroProfile Config):
 * <ul>
 *   <li>{@code minio.endpoint} – MinIO server URL</li>
 *   <li>{@code minio.access-key} – access key / username</li>
 *   <li>{@code minio.secret-key} – secret key / password</li>
 *   <li>{@code minio.bucket} – bucket name to use for all images</li>
 * </ul>
 */
@Slf4j
@ApplicationScoped
public class MinioStorageAdapter implements ImageStoragePort {

    @ConfigProperty(name = "minio.endpoint")
    String endpoint;

    @ConfigProperty(name = "minio.access-key")
    String accessKey;

    @ConfigProperty(name = "minio.secret-key")
    String secretKey;

    @ConfigProperty(name = "minio.bucket")
    String bucket;

    private MinioClient minioClient;

    @PostConstruct
    void init() {
        minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
        ensureBucketExists();
    }

    /**
     * Creates the configured bucket if it does not already exist.
     *
     * @throws IllegalStateException if the MinIO API call fails
     */
    private void ensureBucketExists() {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                log.info("Created MinIO bucket '{}'", bucket);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialise MinIO bucket '" + bucket + "'", e);
        }
    }

    @Override
    public String uploadImage(InputStream stream, long sizeInBytes) {
        String objectKey = UUID.randomUUID().toString();
        try {
            byte[] bytes;
            if (sizeInBytes < 0) {
                bytes = stream.readAllBytes();
                sizeInBytes = bytes.length;
                stream = new ByteArrayInputStream(bytes);
            }
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .stream(stream, sizeInBytes, -1)
                            .contentType("application/octet-stream")
                            .build()
            );
            log.debug("Uploaded image to MinIO with key '{}'", objectKey);
            return objectKey;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to upload image to MinIO", e);
        }
    }

    @Override
    public String generatePresignedUrl(String objectKey) {
        try {
            String url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucket)
                            .object(objectKey)
                            .expiry(60, TimeUnit.MINUTES)
                            .build()
            );
            log.debug("Generated presigned URL for key '{}'", objectKey);
            return url;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate presigned URL for key '" + objectKey + "'", e);
        }
    }

    @Override
    public void deleteImage(String objectKey) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .build()
            );
            log.debug("Deleted image from MinIO with key '{}'", objectKey);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to delete image from MinIO with key '" + objectKey + "'", e);
        }
    }
}
