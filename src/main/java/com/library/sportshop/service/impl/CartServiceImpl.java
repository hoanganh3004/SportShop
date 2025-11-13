package com.library.sportshop.service.impl;

import com.library.sportshop.dto.CartItemResponseDTO;
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
        if (items == null) return dto;
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
}


