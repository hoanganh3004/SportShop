package com.library.sportshop.repository;

import com.library.sportshop.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Integer> {
    
    // Tìm tất cả ảnh của một sản phẩm
    List<ProductImage> findByProductId(Integer productId);
    
    // Xóa tất cả ảnh của một sản phẩm
    @Modifying
    @Transactional
    @Query("DELETE FROM ProductImage pi WHERE pi.product.id = :productId")
    void deleteByProductId(@Param("productId") Integer productId);
    
    // Đếm số ảnh của một sản phẩm
    long countByProductId(Integer productId);
}
