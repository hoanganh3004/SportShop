package com.library.sportshop.repository;

import com.library.sportshop.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    // Giữ nguyên các phương thức cơ bản
    Page<Product> findByNameContaining(String name, Pageable pageable);
    Page<Product> findByMaspContaining(String masp, Pageable pageable);
    Page<Product> findByCategoryId(Integer categoryId, Pageable pageable);
    Page<Product> findByNameContainingOrMaspContaining(String name, String masp, Pageable pageable);
}