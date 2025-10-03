package com.library.sportshop.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private String masp;

    @Column(name = "price")
    private BigDecimal price;

    private String description;
    private String size;
    private String color;
    private Integer quantity;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(mappedBy = "product")
    private List<ProductImage> images;

    @OneToMany(mappedBy = "product")
    private List<CartItem> cartItems;

    @OneToMany(mappedBy = "product")
    private List<OrderItem> orderItems;

    public Product() {}

    public Product(Integer id, String name, String masp, BigDecimal price,
                   String description, String size, String color, Integer quantity,
                   Boolean isDeleted, LocalDateTime createdAt, Category category) {
        this.id = id;
        this.name = name;
        this.masp = masp;
        this.price = price;
        this.description = description;
        this.size = size;
        this.color = color;
        this.quantity = quantity;
        this.isDeleted = isDeleted;
        this.createdAt = createdAt;
        this.category = category;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMasp() {
        return masp;
    }

    public void setMasp(String masp) {
        this.masp = masp;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Boolean getDeleted() {
        return isDeleted;
    }

    public void setDeleted(Boolean deleted) {
        isDeleted = deleted;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public List<ProductImage> getImages() {
        return images;
    }

    public void setImages(List<ProductImage> images) {
        this.images = images;
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    public void setCartItems(List<CartItem> cartItems) {
        this.cartItems = cartItems;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    // Utility method để lấy tên file ảnh đầu tiên
    public String getFirstImageFileName() {
        if (images != null && !images.isEmpty()) {
            String imageUrl = images.get(0).getImageUrl();
            if (imageUrl != null) {
                // Lấy tên file từ đường dẫn đầy đủ (D:\image\s1.jpg -> s1.jpg)
                return imageUrl.substring(imageUrl.lastIndexOf("\\") + 1);
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", masp='" + masp + '\'' +
                ", price=" + price +
                ", description='" + description + '\'' +
                ", size='" + size + '\'' +
                ", color='" + color + '\'' +
                ", quantity=" + quantity +
                ", isDeleted=" + isDeleted +
                ", createdAt=" + createdAt +
                ", category=" + category +
                ", images=" + images +
                ", cartItems=" + cartItems +
                ", orderItems=" + orderItems +
                '}';
    }
}
