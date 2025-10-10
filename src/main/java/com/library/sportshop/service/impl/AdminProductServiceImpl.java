package com.library.sportshop.service.impl;

import com.library.sportshop.entity.Product;
import com.library.sportshop.entity.ProductImage;
import com.library.sportshop.repository.ProductRepository;
import com.library.sportshop.repository.ProductImageRepository;
import com.library.sportshop.service.AdminProductService;
import com.library.sportshop.service.FileUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class AdminProductServiceImpl implements AdminProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductImageRepository productImageRepository;

    @Autowired
    private FileUploadService fileUploadService;

    @Override
    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    //  DÙNG CHO DROPDOWN
    @Override
    public Page<Product> getAllProductsForDropdown() {
        Pageable pageable = PageRequest.of(0, 100); // load tối đa 100 sản phẩm cho dropdown
        return productRepository.findAll(pageable);
    }

    @Override
    public Page<Product> searchProducts(String keyword, Integer categoryId, Pageable pageable) {
        if (categoryId != null && categoryId > 0) {
            return productRepository.findByCategoryId(categoryId, pageable);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            String searchKeyword = keyword.trim();
            return productRepository.findByNameContainingOrMaspContaining(searchKeyword, searchKeyword, pageable);
        }
        return productRepository.findAll(pageable);
    }

    @Override
    public Product getProductById(Integer id) {
        return productRepository.findById(id).orElse(null);
    }

    @Override
    public void saveProduct(Product product) {
        productRepository.save(product);
    }

    @Override
    public void deleteProduct(Integer id) {
        productRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Product getProductByIdWithImages(Integer id) {
        Product product = productRepository.findById(id).orElse(null);
        if (product != null && product.getImages() != null) {
            product.getImages().size();
        }
        return product;
    }

    @Override
    @Transactional
    public void saveProductWithImages(Product product, MultipartFile[] images) {
        try {
            if (product.getId() == null) {
                product.setCreatedAt(LocalDateTime.now());
            }
            Product savedProduct = productRepository.save(product);
            if (images != null && images.length > 0) {
                List<String> uploadedPaths = fileUploadService.uploadMultipleFiles(images);
                for (String imagePath : uploadedPaths) {
                    ProductImage productImage = new ProductImage();
                    productImage.setImageUrl(imagePath);
                    productImage.setProduct(savedProduct);
                    productImage.setCreatedAt(LocalDateTime.now());
                    productImageRepository.save(productImage);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi lưu sản phẩm và ảnh: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void updateProductWithImages(Product product, MultipartFile[] images) {
        try {
            Product existingProduct = productRepository.findById(product.getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + product.getId()));

            existingProduct.setName(product.getName());
            existingProduct.setMasp(product.getMasp());
            existingProduct.setPrice(product.getPrice());
            existingProduct.setDescription(product.getDescription());
            existingProduct.setSize(product.getSize());
            existingProduct.setColor(product.getColor());
            existingProduct.setQuantity(product.getQuantity());
            if (product.getCategory() != null) {
                existingProduct.setCategory(product.getCategory());
            }

            Product savedProduct = productRepository.save(existingProduct);

            if (images != null && images.length > 0) {
                List<ProductImage> oldImages = productImageRepository.findByProductId(savedProduct.getId());
                productImageRepository.deleteByProductId(savedProduct.getId());
                for (ProductImage oldImage : oldImages) {
                    fileUploadService.deleteFile(oldImage.getImageUrl());
                }
                List<String> uploadedPaths = fileUploadService.uploadMultipleFiles(images);
                for (String imagePath : uploadedPaths) {
                    ProductImage productImage = new ProductImage();
                    productImage.setImageUrl(imagePath);
                    productImage.setProduct(savedProduct);
                    productImage.setCreatedAt(LocalDateTime.now());
                    productImageRepository.save(productImage);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi cập nhật sản phẩm và ảnh: " + e.getMessage(), e);
        }
    }
}
