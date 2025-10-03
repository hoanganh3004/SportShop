package com.library.sportshop.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_images")
public class ProductImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    public ProductImage() {}

    public ProductImage(Integer id, String imageUrl, LocalDateTime createdAt, Product product) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.createdAt = createdAt;
        this.product = product;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    // Utility method để lấy tên file từ đường dẫn đầy đủ
    public String getFileName() {
        if (imageUrl != null) {
            // Lấy tên file từ đường dẫn đầy đủ (D:\image\s1.jpg -> s1.jpg)
            return imageUrl.substring(imageUrl.lastIndexOf("\\") + 1);
        }
        return null;
    }

    @Override
    public String toString() {
        return "ProductImage{" +
                "id=" + id +
                ", imageUrl='" + imageUrl + '\'' +
                ", createdAt=" + createdAt +
                ", product=" + product +
                '}';
    }
}
