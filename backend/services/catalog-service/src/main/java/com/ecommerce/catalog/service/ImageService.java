package com.ecommerce.catalog.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.catalog.dto.CreateImageRequest;
import com.ecommerce.catalog.dto.ImageDto;
import com.ecommerce.catalog.entity.Image;
import com.ecommerce.catalog.entity.Product;
import com.ecommerce.catalog.repository.ImageRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class ImageService {

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private S3Service s3Service;

    /**
     * Create a new image for a product
     */
    public ImageDto createImage(Product product, CreateImageRequest request) {
        log.info("Creating image for product {} with S3 key: {}", product.getId(), request.getS3Key());
        
        // Check if S3 key already exists
        if (imageRepository.existsByS3Key(request.getS3Key())) {
            throw new IllegalArgumentException("Image with S3 key already exists: " + request.getS3Key());
        }
        
        // Use public URL for MinIO (bucket is public), or presigned URL for private buckets
        String downloadUrl = s3Service.getPublicUrl(request.getS3Key());
        
        // Extract metadata from S3 key
        String bucket = extractBucketFromKey(request.getS3Key());
        String fileName = extractFileNameFromKey(request.getS3Key());
        
        Image image = Image.builder()
                .s3Key(request.getS3Key())
                .s3Bucket(bucket)
                .url(downloadUrl)
                .fileName(fileName)
                .altText(request.getAltText())
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .isPrimary(request.getIsPrimary() != null ? request.getIsPrimary() : false)
                .isActive(true)
                .product(product)
                .build();
        
        Image savedImage = imageRepository.save(image);
        log.info("Successfully created image with ID: {}", savedImage.getId());
        
        return convertToDto(savedImage);
    }

    /**
     * Get all images for a product
     */
    @Transactional(readOnly = true)
    public List<ImageDto> getImagesByProductId(Long productId) {
        log.debug("Fetching images for product: {}", productId);
        List<Image> images = imageRepository.findActiveImagesByProductId(productId);
        return images.stream().map(this::convertToDto).toList();
    }

    /**
     * Get primary image for a product
     */
    @Transactional(readOnly = true)
    public Optional<ImageDto> getPrimaryImageByProductId(Long productId) {
        log.debug("Fetching primary image for product: {}", productId);
        Optional<Image> primaryImage = imageRepository.findPrimaryImageByProductId(productId);
        return primaryImage.map(this::convertToDto);
    }

    /**
     * Update image
     */
    public ImageDto updateImage(Long imageId, CreateImageRequest request) {
        log.info("Updating image: {}", imageId);
        
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new IllegalArgumentException("Image not found: " + imageId));
        
        // Update fields if provided
        if (request.getAltText() != null) {
            image.setAltText(request.getAltText());
        }
        if (request.getDisplayOrder() != null) {
            image.setDisplayOrder(request.getDisplayOrder());
        }
        if (request.getIsPrimary() != null) {
            // If setting as primary, unset other primary images for the same product
            if (request.getIsPrimary()) {
                unsetPrimaryForProduct(image.getProduct().getId(), imageId);
            }
            image.setIsPrimary(request.getIsPrimary());
        }
        
        Image savedImage = imageRepository.save(image);
        log.info("Successfully updated image: {}", imageId);
        
        return convertToDto(savedImage);
    }

    /**
     * Delete image
     */
    public void deleteImage(Long imageId) {
        log.info("Deleting image: {}", imageId);
        
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new IllegalArgumentException("Image not found: " + imageId));
        
        // TODO: Optionally delete from S3 as well
        // s3Service.deleteObject(image.getS3Key());
        
        imageRepository.delete(image);
        log.info("Successfully deleted image: {}", imageId);
    }

    /**
     * Delete all images for a product
     */
    public void deleteImagesByProductId(Long productId) {
        log.info("Deleting all images for product: {}", productId);
        List<Image> images = imageRepository.findByProductIdOrderByDisplayOrderAscIdAsc(productId);
        
        // TODO: Optionally delete from S3 as well
        // images.forEach(image -> s3Service.deleteObject(image.getS3Key()));
        
        imageRepository.deleteByProductId(productId);
        log.info("Successfully deleted {} images for product: {}", images.size(), productId);
    }

    /**
     * Set image as primary for a product
     */
    public void setPrimaryImage(Long imageId) {
        log.info("Setting image as primary: {}", imageId);
        
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new IllegalArgumentException("Image not found: " + imageId));
        
        // Unset other primary images for the same product
        unsetPrimaryForProduct(image.getProduct().getId(), imageId);
        
        // Set this image as primary
        image.setIsPrimary(true);
        imageRepository.save(image);
        
        log.info("Successfully set image {} as primary", imageId);
    }

    /**
     * Helper method to unset primary flag for other images of the same product
     */
    private void unsetPrimaryForProduct(Long productId, Long excludeImageId) {
        List<Image> images = imageRepository.findByProductIdOrderByDisplayOrderAscIdAsc(productId);
        images.stream()
                .filter(img -> !img.getId().equals(excludeImageId))
                .filter(Image::getIsPrimary)
                .forEach(img -> {
                    img.setIsPrimary(false);
                    imageRepository.save(img);
                });
    }

    /**
     * Convert Image entity to DTO
     */
    private ImageDto convertToDto(Image image) {
        ImageDto dto = new ImageDto();
        dto.setId(image.getId());
        dto.setS3Key(image.getS3Key());
        dto.setS3Bucket(image.getS3Bucket());
        dto.setUrl(image.getUrl());
        dto.setFileName(image.getFileName());
        dto.setContentType(image.getContentType());
        dto.setFileSize(image.getFileSize());
        dto.setAltText(image.getAltText());
        dto.setDisplayOrder(image.getDisplayOrder());
        dto.setIsPrimary(image.getIsPrimary());
        dto.setIsActive(image.getIsActive());
        dto.setCreatedAt(image.getCreatedAt());
        dto.setUpdatedAt(image.getUpdatedAt());
        return dto;
    }

    /**
     * Extract bucket name from S3 key (assuming key format includes bucket info)
     */
    private String extractBucketFromKey(String s3Key) {
        // This depends on your S3 structure. 
        // For now, return a default bucket name from configuration
        return "ecommerce-images"; // This should come from application properties
    }

    /**
     * Extract file name from S3 key
     */
    private String extractFileNameFromKey(String s3Key) {
        return s3Key.substring(s3Key.lastIndexOf('/') + 1);
    }
}