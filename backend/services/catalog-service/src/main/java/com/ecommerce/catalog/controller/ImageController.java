package com.ecommerce.catalog.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.catalog.dto.BulkUploadRequest;
import com.ecommerce.catalog.dto.BulkUploadResponse;
import com.ecommerce.catalog.service.S3Service;
import com.ecommerce.catalog.service.S3Service.PresignedUploadUrlResponse;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {

    private final S3Service s3Service;

    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
        "image/jpeg",
        "image/jpg", 
        "image/png",
        "image/gif",
        "image/webp"
    );

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final int MAX_BULK_FILES = 20; // Maximum files per bulk request

    /**
     * Generate a presigned URL for uploading product images
     */
    @PostMapping("/upload-url")
    public ResponseEntity<PresignedUploadUrlResponse> getUploadUrl(
            @RequestParam @NotBlank String fileName,
            @RequestParam @NotBlank @Pattern(regexp = "image/(jpeg|jpg|png|gif|webp)", 
                message = "Content type must be a valid image type") String contentType,
            @RequestParam(required = false) Long fileSize) {
        
        log.info("Generating upload URL for file: {} with content type: {}", fileName, contentType);
        
        // Validate content type
        if (!ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            log.warn("Invalid content type requested: {}", contentType);
            return ResponseEntity.badRequest().build();
        }
        
        // Validate file size if provided
        if (fileSize != null && fileSize > MAX_FILE_SIZE) {
            log.warn("File size too large: {} bytes", fileSize);
            return ResponseEntity.badRequest().build();
        }
        
        try {
            PresignedUploadUrlResponse response = s3Service.generatePresignedUploadUrl(fileName, contentType);
            log.info("Successfully generated upload URL for key: {}", response.key());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error generating upload URL for file: {}", fileName, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Generate presigned URLs for multiple file uploads (bulk operation)
     */
    @PostMapping("/bulk-upload-urls")
    public ResponseEntity<BulkUploadResponse> getBulkUploadUrls(@Valid @RequestBody BulkUploadRequest request) {
        log.info("Generating bulk upload URLs for {} files", request.files().size());
        
        // Validate bulk request limits
        if (request.files().size() > MAX_BULK_FILES) {
            log.warn("Too many files in bulk request: {}", request.files().size());
            return ResponseEntity.badRequest().build();
        }
        
        List<PresignedUploadUrlResponse> uploadUrls = request.files().stream()
            .map(fileRequest -> {
                try {
                    // Validate each file
                    if (!ALLOWED_CONTENT_TYPES.contains(fileRequest.contentType().toLowerCase())) {
                        log.warn("Invalid content type in bulk request: {}", fileRequest.contentType());
                        throw new IllegalArgumentException("Invalid content type: " + fileRequest.contentType());
                    }
                    
                    if (fileRequest.fileSize() != null && fileRequest.fileSize() > MAX_FILE_SIZE) {
                        log.warn("File size too large in bulk request: {} bytes", fileRequest.fileSize());
                        throw new IllegalArgumentException("File size too large: " + fileRequest.fileSize());
                    }
                    
                    return s3Service.generatePresignedUploadUrl(fileRequest.fileName(), fileRequest.contentType());
                } catch (Exception e) {
                    log.error("Error generating upload URL for file: {}", fileRequest.fileName(), e);
                    throw new RuntimeException("Failed to generate URL for file: " + fileRequest.fileName(), e);
                }
            })
            .collect(Collectors.toList());
        
        BulkUploadResponse response = new BulkUploadResponse(uploadUrls);
        log.info("Successfully generated {} upload URLs", uploadUrls.size());
        return ResponseEntity.ok(response);
    }

    /**
     * Generate a presigned URL for downloading/viewing an image
     */
    @GetMapping("/download-url/{key:.+}")
    public ResponseEntity<Map<String, String>> getDownloadUrl(@PathVariable String key) {
        log.info("Generating download URL for key: {}", key);
        
        try {
            String downloadUrl = s3Service.generatePresignedDownloadUrl(key);
            log.info("Successfully generated download URL for key: {}", key);
            return ResponseEntity.ok(Map.of(
                "downloadUrl", downloadUrl,
                "key", key
            ));
        } catch (Exception e) {
            log.error("Error generating download URL for key: {}", key, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}