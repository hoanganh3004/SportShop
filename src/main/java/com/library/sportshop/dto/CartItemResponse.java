package com.library.sportshop.dto;

import java.math.BigDecimal;

public class CartItemResponse {
    private Integer id;
    private Integer quantity;
    private ProductInfo product;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public ProductInfo getProduct() { return product; }
    public void setProduct(ProductInfo product) { this.product = product; }

    public static class ProductInfo {
        private Integer id;
        private String name;
        private BigDecimal price;
        private String firstImageFileName;

        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        public String getFirstImageFileName() { return firstImageFileName; }
        public void setFirstImageFileName(String firstImageFileName) { this.firstImageFileName = firstImageFileName; }
    }
}


