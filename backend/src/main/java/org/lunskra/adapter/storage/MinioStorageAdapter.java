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
import okhttp3.OkHttpClient;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.lunskra.port.out.ImageStoragePort;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@ApplicationScoped
public class MinioStorageAdapter implements ImageStoragePort {

    @ConfigProperty(name = "minio.endpoint")
    String endpoint;

    @ConfigProperty(name = "minio.public-endpoint")
    String publicEndpoint;

    @ConfigProperty(name = "minio.access-key")
    String accessKey;

    @ConfigProperty(name = "minio.secret-key")
    String secretKey;

    @ConfigProperty(name = "minio.bucket")
    String bucket;

    @ConfigProperty(name = "minio.region", defaultValue = "us-east-1")
    String region;

    private MinioClient minioClient;
    private MinioClient minioPublicClient;

    @PostConstruct
    void init() {
        minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .region(region)
                .build();

        // The public client signs URLs with the public endpoint (e.g. localhost:9000)
        // so browsers can reach them, but routes its own network calls (region lookup)
        // through the internal endpoint via a custom DNS resolver.
        String internalHost = URI.create(endpoint).getHost();
        OkHttpClient routingHttpClient = new OkHttpClient.Builder()
                .dns(hostname -> okhttp3.Dns.SYSTEM.lookup(internalHost))
                .build();
        minioPublicClient = MinioClient.builder()
                .endpoint(publicEndpoint)
                .credentials(accessKey, secretKey)
                .region(region)
                .httpClient(routingHttpClient)
                .build();

        ensureBucketExists();
    }

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
            String url = minioPublicClient.getPresignedObjectUrl(
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
