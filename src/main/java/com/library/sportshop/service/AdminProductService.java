package com.library.sportshop.service;

import com.library.sportshop.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface AdminProductService {
    Page<Product> getAllProducts(Pageable pageable);

    Page<Product> searchProducts(String keyword, Integer categoryId, Pageable pageable);

    Product getProductById(Integer id);

    void saveProduct(Product product);

    void deleteProduct(Integer id);

    Product getProductByIdWithImages(Integer id);

    void saveProductWithImages(Product product, MultipartFile[] images);

    void updateProductWithImages(Product product, MultipartFile[] images);

    void deleteImage(Integer imageId);

    Page<Product> getAllProductsForDropdown();

    // === Methods cho user product pages ===

    // Đếm tổng số sản phẩm
    long countProducts();

    // Tìm sản phẩm với filter (cho user product page)
    Page<Product> findByFilters(String name, Long minPrice, Long maxPrice, Integer categoryId, Pageable pageable);

    // Tìm sản phẩm theo category
    Page<Product> findByCategoryId(Integer categoryId, Pageable pageable);

    // Tìm sản phẩm theo tên hoặc mã
    Page<Product> findByNameOrMasp(String keyword, Pageable pageable);
}
