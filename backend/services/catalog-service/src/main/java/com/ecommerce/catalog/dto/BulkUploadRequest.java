package com.ecommerce.catalog.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record BulkUploadRequest(
    @NotEmpty(message = "Files list cannot be empty")
    @Size(max = 20, message = "Maximum 20 files allowed per bulk request")
    @Valid
    List<FileUploadRequest> files
) {}