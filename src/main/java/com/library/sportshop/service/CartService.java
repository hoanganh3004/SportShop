package com.library.sportshop.service;

import com.library.sportshop.dto.CartItemResponseDTO;
import com.library.sportshop.entity.CartItem;

import java.util.List;

public interface CartService {
    List<CartItem> findByUserCode(String userCode);

    Integer countQuantityByUserCode(String userCode);

    List<CartItemResponseDTO> mapToDto(List<CartItem> items);

    Integer addToCart(Integer productId, Integer quantity, String userCode);

    Integer increaseQuantity(Integer productId, String userCode);

    Integer decreaseQuantity(Integer productId, String userCode);

    void removeItem(Integer productId, String userCode);
}
