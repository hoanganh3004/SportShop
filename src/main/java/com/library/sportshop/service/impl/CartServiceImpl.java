package com.library.sportshop.service.impl;

import com.library.sportshop.dto.CartItemResponse;
import com.library.sportshop.entity.CartItem;
import com.library.sportshop.repository.CartItemRepository;
import com.library.sportshop.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Override
    public List<CartItem> findByUserCode(String userCode) {
        return cartItemRepository.findByUserCode(userCode);
    }

    @Override
    public Integer countQuantityByUserCode(String userCode) {
        return cartItemRepository.countQuantityByUserCode(userCode);
    }

    @Override
    public List<CartItemResponse> mapToDto(List<CartItem> items) {
        List<CartItemResponse> dto = new ArrayList<>();
        if (items == null) return dto;
        for (CartItem it : items) {
            CartItemResponse r = new CartItemResponse();
            r.setId(it.getId());
            r.setQuantity(it.getQuantity());
            if (it.getProduct() != null) {
                CartItemResponse.ProductInfo p = new CartItemResponse.ProductInfo();
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
}


