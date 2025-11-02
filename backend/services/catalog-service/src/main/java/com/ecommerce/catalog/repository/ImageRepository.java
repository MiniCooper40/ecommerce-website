package com.ecommerce.catalog.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ecommerce.catalog.entity.Image;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {

    /**
     * Find all images for a specific product
     */
    List<Image> findByProductIdOrderByDisplayOrderAscIdAsc(Long productId);

    /**
     * Find all active images for a specific product
     */
    @Query("SELECT i FROM Image i WHERE i.product.id = :productId AND i.isActive = true ORDER BY i.displayOrder ASC, i.id ASC")
    List<Image> findActiveImagesByProductId(@Param("productId") Long productId);

    /**
     * Find the primary image for a specific product
     */
    @Query("SELECT i FROM Image i WHERE i.product.id = :productId AND i.isPrimary = true AND i.isActive = true")
    Optional<Image> findPrimaryImageByProductId(@Param("productId") Long productId);

    /**
     * Find image by S3 key
     */
    Optional<Image> findByS3Key(String s3Key);

    /**
     * Check if an S3 key already exists
     */
    boolean existsByS3Key(String s3Key);

    /**
     * Find all images by product IDs (for bulk operations)
     */
    @Query("SELECT i FROM Image i WHERE i.product.id IN :productIds AND i.isActive = true ORDER BY i.product.id, i.displayOrder ASC, i.id ASC")
    List<Image> findActiveImagesByProductIds(@Param("productIds") List<Long> productIds);

    /**
     * Delete all images for a specific product
     */
    void deleteByProductId(Long productId);

    /**
     * Count active images for a product
     */
    @Query("SELECT COUNT(i) FROM Image i WHERE i.product.id = :productId AND i.isActive = true")
    long countActiveImagesByProductId(@Param("productId") Long productId);
}