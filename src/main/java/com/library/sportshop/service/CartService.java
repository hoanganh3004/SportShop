package com.library.sportshop.service;

import com.library.sportshop.dto.CartItemResponseDTO;
import com.library.sportshop.entity.CartItem;

import java.util.List;

public interface CartService {
    List<CartItem> findByUserCode(String userCode);
    Integer countQuantityByUserCode(String userCode);
    List<CartItemResponseDTO> mapToDto(List<CartItem> items);
}
