package com.ecommerce.cart.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ecommerce.cart.entity.CartItemView;

/**
 * Repository for CartItemView (read model).
 */
@Repository
public interface CartItemViewRepository extends JpaRepository<CartItemView, Long> {

    List<CartItemView> findByUserId(String userId);

    List<CartItemView> findByCartId(String cartId);

    void deleteByCartItemId(Long cartItemId);

    void deleteByUserId(String userId);

    CartItemView findByCartItemId(Long cartItemId);

    List<CartItemView> findByProductId(Long productId);

    @Query("SELECT COALESCE(SUM(v.quantity), 0) FROM CartItemView v WHERE v.userId = :userId")
    Integer sumQuantityByUserId(@Param("userId") String userId);

    @Modifying
    @Query("UPDATE CartItemView v SET v.productName = :name, v.productDescription = :description, " +
           "v.productPrice = :price, v.productImageUrl = :imageUrl, v.productCategory = :category, " +
           "v.productActive = :active WHERE v.productId = :productId")
    void updateProductDetailsForProduct(@Param("productId") Long productId,
                                       @Param("name") String name,
                                       @Param("description") String description,
                                       @Param("price") java.math.BigDecimal price,
                                       @Param("imageUrl") String imageUrl,
                                       @Param("category") String category,
                                       @Param("active") Boolean active);

    @Modifying
    @Query("UPDATE CartItemView v SET v.available = false WHERE v.productId = :productId")
    void markProductAsUnavailable(@Param("productId") Long productId);

    @Modifying
    @Query("DELETE FROM CartItemView v WHERE v.productId = :productId AND v.available = false")
    void deleteUnavailableProductItems(@Param("productId") Long productId);
}
