package com.ecommerce.catalog.dto;

import java.util.List;

import com.ecommerce.catalog.service.S3Service.PresignedUploadUrlResponse;

public record BulkUploadResponse(
    List<PresignedUploadUrlResponse> uploadUrls
) {}