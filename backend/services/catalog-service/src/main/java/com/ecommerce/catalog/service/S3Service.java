package com.ecommerce.catalog.service;

import java.time.Duration;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Slf4j
@Service
public class S3Service {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Value("${aws.s3.endpoint:}")
    private String s3Endpoint;

    public S3Service(S3Client s3Client, S3Presigner s3Presigner) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
    }

    /**
     * Generate a presigned URL for uploading a file
     */
    public PresignedUploadUrlResponse generatePresignedUploadUrl(String fileName, String contentType) {
        String key = generateObjectKey(fileName);
        
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(15))
                .putObjectRequest(putObjectRequest)
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
        
        log.info("Generated presigned upload URL for key: {}", key);
        
        return new PresignedUploadUrlResponse(
            presignedRequest.url().toString(),
            key,
            bucketName,
            Duration.ofMinutes(15).toMinutes()
        );
    }

    /**
     * Generate a presigned URL for downloading/viewing a file
     */
    public String generatePresignedDownloadUrl(String key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofHours(1))
                .getObjectRequest(getObjectRequest)
                .build();

        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
        
        log.info("Generated presigned download URL for key: {}", key);
        
        return presignedRequest.url().toString();
    }

    /**
     * Get the public URL for an object (for public buckets)
     */
    public String getPublicUrl(String key) {
        if (!s3Endpoint.isEmpty()) {
            // For MinIO/local development
            return String.format("%s/%s/%s", s3Endpoint, bucketName, key);
        } else {
            // For AWS S3
            return String.format("https://%s.s3.amazonaws.com/%s", bucketName, key);
        }
    }

    /**
     * Generate a unique object key for file storage
     */
    private String generateObjectKey(String fileName) {
        String fileExtension = "";
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            fileExtension = fileName.substring(lastDotIndex);
        }
        
        return String.format("products/%s/%s%s", 
            UUID.randomUUID().toString(), 
            System.currentTimeMillis(), 
            fileExtension);
    }

    /**
     * Response object for presigned upload URL
     */
    public record PresignedUploadUrlResponse(
        String uploadUrl,
        String key,
        String bucket,
        long expiresInMinutes
    ) {}
}