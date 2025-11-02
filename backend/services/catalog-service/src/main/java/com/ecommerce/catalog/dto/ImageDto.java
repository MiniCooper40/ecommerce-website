package com.ecommerce.catalog.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageDto {
    
    private Long id;
    
    @NotBlank(message = "S3 key is required")
    private String s3Key;
    
    @NotBlank(message = "S3 bucket is required")
    private String s3Bucket;
    
    @NotBlank(message = "Image URL is required")
    private String url;
    
    private String fileName;
    
    private String contentType;
    
    private Long fileSize;
    
    private String altText;
    
    @NotNull(message = "Display order is required")
    private Integer displayOrder;
    
    private Boolean isPrimary;
    
    private Boolean isActive;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}