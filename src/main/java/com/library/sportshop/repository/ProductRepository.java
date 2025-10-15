package com.library.sportshop.repository;

import com.library.sportshop.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    // Giữ nguyên các phương thức cơ bản
    Page<Product> findByNameContaining(String name, Pageable pageable);
    Page<Product> findByMaspContaining(String masp, Pageable pageable);
    Page<Product> findByCategoryId(Integer categoryId, Pageable pageable);
    Page<Product> findByNameContainingOrMaspContaining(String name, String masp, Pageable pageable);

    // Trừ kho có điều kiện: chỉ trừ khi đủ hàng, trả về số bản ghi cập nhật (0 hoặc 1)
    @Modifying
    @Query("update Product p set p.quantity = p.quantity - :qty where p.id = :id and p.quantity >= :qty")
    int decrementIfEnough(@Param("id") Integer id, @Param("qty") Integer qty);
}