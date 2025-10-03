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

    Page<Product> getAllProductsForDropdown();
}
