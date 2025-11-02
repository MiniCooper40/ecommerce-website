package com.ecommerce.catalog.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateImageRequest {
    
    @NotBlank(message = "S3 key is required")
    private String s3Key;
    
    private String altText;
    
    private Integer displayOrder;
    
    private Boolean isPrimary;
}