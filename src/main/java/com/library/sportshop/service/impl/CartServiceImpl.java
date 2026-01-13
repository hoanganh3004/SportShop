package com.library.sportshop.service.impl;

import com.library.sportshop.dto.CartItemResponseDTO;
import com.library.sportshop.entity.CartItem;
import com.library.sportshop.entity.Product;
import com.library.sportshop.repository.CartItemRepository;
import com.library.sportshop.repository.ProductRepository;
import com.library.sportshop.service.CartService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Override
    // lấy danh sách item trong giỏ theo mã người dùng
    public List<CartItem> findByUserCode(String userCode) {
        return cartItemRepository.findByUserCode(userCode);
    }

    @Override
    // đếm tổng số lượng sản phẩm trong giỏ theo mã người dùng
    public Integer countQuantityByUserCode(String userCode) {
        return cartItemRepository.countQuantityByUserCode(userCode);
    }

    @Override
    // ánh xạ danh sách CartItem -> CartItemResponseDTO để trả về client
    public List<CartItemResponseDTO> mapToDto(List<CartItem> items) {
        List<CartItemResponseDTO> dto = new ArrayList<>();
        // nếu danh sách rỗng thì trả về danh sách DTO rỗng
        if (items == null)
            return dto;
        for (CartItem it : items) {
            // tạo DTO cho từng item trong giỏ
            CartItemResponseDTO r = new CartItemResponseDTO();
            r.setId(it.getId());
            r.setQuantity(it.getQuantity());
            // gắn thông tin sản phẩm nếu có
            if (it.getProduct() != null) {
                CartItemResponseDTO.ProductInfo p = new CartItemResponseDTO.ProductInfo();
                p.setId(it.getProduct().getId());
                p.setName(it.getProduct().getName());
                p.setPrice(it.getProduct().getPrice());
                p.setFirstImageFileName(it.getProduct().getFirstImageFileName());
                r.setProduct(p);
            }
            dto.add(r);
        }
        return dto;
    }

    @Autowired
    private ProductRepository productRepository;

    @Override
    @Transactional
    public Integer addToCart(Integer productId, Integer quantity, String userCode) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) {
            throw new RuntimeException("INVALID_PRODUCT");
        }

        Integer stock = product.getQuantity();
        if (stock == null)
            stock = 0;

        // ktra sp có chưa
        CartItem existingItem = cartItemRepository.findByUserCodeAndProduct_Id(userCode, productId).orElse(null);
        int currentInCart = (existingItem != null) ? existingItem.getQuantity() : 0;
        int newTotal = currentInCart + quantity;

        // check tồn ko
        if (stock <= 0) {
            throw new RuntimeException("OUT_OF_STOCK");
        }
        if (newTotal > stock) {
            throw new RuntimeException("INSUFFICIENT_STOCK");
        }

        CartItem item = existingItem != null ? existingItem : new CartItem();
        if (existingItem == null) {
            item.setUserCode(userCode);
            item.setProduct(product);
            item.setQuantity(0);
        }

        item.setQuantity(item.getQuantity() + quantity);
        cartItemRepository.save(item);

        return cartItemRepository.countQuantityByUserCode(userCode);
    }

    @Override
    @Transactional
    public Integer increaseQuantity(Integer productId, String userCode) {
        CartItem item = cartItemRepository.findByUserCodeAndProduct_Id(userCode, productId).orElse(null);
        if (item == null) {
            throw new RuntimeException("ITEM_NOT_FOUND");
        }

        com.library.sportshop.entity.Product product = productRepository.findById(productId).orElse(null);
        if (product == null) {
            throw new RuntimeException("INVALID_PRODUCT");
        }

        Integer stock = product.getQuantity() == null ? Integer.MAX_VALUE : product.getQuantity();
        if (item.getQuantity() >= stock) {
            throw new RuntimeException("LIMIT_REACHED");
        }

        item.setQuantity(item.getQuantity() + 1);
        cartItemRepository.save(item);

        return cartItemRepository.countQuantityByUserCode(userCode);
    }

    @Override
    @Transactional
    public Integer decreaseQuantity(Integer productId, String userCode) {
        CartItem item = cartItemRepository.findByUserCodeAndProduct_Id(userCode, productId).orElse(null);
        if (item == null) {
            throw new RuntimeException("ITEM_NOT_FOUND");
        }

        if (item.getQuantity() > 1) {
            item.setQuantity(item.getQuantity() - 1);
            cartItemRepository.save(item);
        }

        return cartItemRepository.countQuantityByUserCode(userCode);
    }

    @Override
    @Transactional
    public void removeItem(Integer productId, String userCode) {
        cartItemRepository.deleteByUserCodeAndProduct_Id(userCode, productId);
    }
}
