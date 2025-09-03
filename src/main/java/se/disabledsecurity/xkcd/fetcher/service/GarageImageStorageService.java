package se.disabledsecurity.xkcd.fetcher.service;

import io.minio.BucketExistsArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import se.disabledsecurity.xkcd.fetcher.common.GarageProperties;
import se.disabledsecurity.xkcd.fetcher.entity.Comic;
import se.disabledsecurity.xkcd.fetcher.exception.ImageStorageException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.atomic.AtomicBoolean;
import io.minio.http.Method;

/**
 * MinIO/Garage-based storage service for XKCD images.
 */
@Slf4j
@Service
public class GarageImageStorageService implements ImageStorageService {

    private final MinioClient minioClient;
    private final GarageProperties garageProperties;
    private final ImageDetectionService imageDetectionService;
    private final AtomicBoolean bucketEnsured = new AtomicBoolean(false);

    public GarageImageStorageService(MinioClient minioClient,
                                     GarageProperties garageProperties,
                                     ImageDetectionService imageDetectionService) {
        this.minioClient = minioClient;
        this.garageProperties = garageProperties;
        this.imageDetectionService = imageDetectionService;
    }

    @Override
    public String save(String key, byte[] content, String contentType) {
        return Try.run(this::ensureBucketExistsOnce)
                .flatMap(ignored ->
                        Try.withResources(() -> new ByteArrayInputStream(content))
                                .of(bais -> {
                                    String detectedContentType = imageDetectionService
                                            .detectContentType(content, contentType, key);

                                    var args = PutObjectArgs.builder()
                                            .bucket(garageProperties.getBucket())
                                            .object(key)
                                            .contentType(detectedContentType)
                                            .stream(bais, content.length, -1)
                                            .build();
                                    minioClient.putObject(args);
                                    log.debug("Stored image '{}' in bucket '{}' with content type '{}'",
                                            key, garageProperties.getBucket(), detectedContentType);
                                    return key;
                                })
                )
                .getOrElseThrow(e -> new ImageStorageException("Failed to store image: " + key, e));
    }

    @Override
    public String save(String key, byte[] content) {
        return save(key, content, null);
    }

    @Override
    public void delete(String key) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(garageProperties.getBucket())
                            .object(key)
                            .build());
            log.debug("Deleted image '{}' from bucket '{}'", key, garageProperties.getBucket());
        } catch (Exception e) {
            log.error("Failed to delete image '{}' from bucket '{}': {}", key, garageProperties.getBucket(), e.getMessage());
            throw new ImageStorageException("Failed to delete image: " + key, e);
        }
    }

    @Override
    public boolean exists(String key) {
        return Try.of(() -> {
                    StatObjectResponse response = minioClient.statObject(
                            StatObjectArgs.builder()
                                    .bucket(garageProperties.getBucket())
                                    .object(key)
                                    .build());
                    return response.size() > 0;
                })
                .recover(ErrorResponseException.class, e -> {
                    String code = e.errorResponse() != null ? e.errorResponse().code() : null;
                    int status = e.response() != null ? e.response().code() : -1;
                    if ("NoSuchKey".equals(code) || status == 404) {
                        return false;
                    }
                    log.warn("Failed to check if object exists '{}': {}", key, e.getMessage());
                    return false;
                })
                .recover(throwable -> {
                    log.warn("Failed to check object existence '{}': {}", key, throwable.getMessage());
                    return false;
                })
                .get();
    }

    @Override
    public String url(String key) {
        return Try.of(() -> minioClient.getPresignedObjectUrl(
                        GetPresignedObjectUrlArgs.builder()
                                .method(Method.GET)
                                .bucket(garageProperties.getBucket())
                                .object(key)
                                .build()))
                .onFailure(e -> log.warn("Failed to get presigned URL for '{}': {}", key, e.getMessage()))
                .getOrElse(() -> buildDirectUrl(key));
    }

    private String buildDirectUrl(String key) {
        String endpoint = garageProperties.getEndpoint() != null ?
                garageProperties.getEndpoint().toString() : "";

        if (endpoint.isEmpty()) {
            return key;
        }

        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(endpoint);

        // Remove trailing slash if present
        if (endpoint.endsWith("/")) {
            urlBuilder.setLength(urlBuilder.length() - 1);
        }

        urlBuilder.append("/")
                .append(garageProperties.getBucket())
                .append("/")
                .append(key);

        return urlBuilder.toString();
    }

    private void ensureBucketExistsOnce() throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        if (bucketEnsured.get()) {
            return;
        }

        synchronized (bucketEnsured) {
            if (bucketEnsured.get()) {
                return;
            }

            String bucket = garageProperties.getBucket();

            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucket).build());

            if (!exists) {
                log.error("Bucket '{}' does not exist and cannot be created automatically with Garage. " +
                        "Please create the bucket through the Garage admin interface.", bucket);
                throw new IllegalStateException("Bucket '" + bucket + "' does not exist. " +
                        "Buckets must be created manually in Garage.");
            }

            log.debug("Bucket '{}' exists and is ready for use", bucket);
            bucketEnsured.set(true);
        }
    }
    public boolean imageExistsForComic(Comic comic) {
        String imageKey = "xkcd/" + comic.getComicNumber();
        return exists(imageKey);
    }
}