package com.ecommerce.catalog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public record FileUploadRequest(
    @NotBlank(message = "File name cannot be blank")
    String fileName,
    
    @NotBlank(message = "Content type cannot be blank")
    @Pattern(regexp = "image/(jpeg|jpg|png|gif|webp)", 
        message = "Content type must be a valid image type")
    String contentType,
    
    @Positive(message = "File size must be positive")
    Long fileSize
) {}