package com.library.sportshop.service;

import com.library.sportshop.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface OrderService {
    Page<Order> getAllOrders(String keyword, Pageable pageable);

    Optional<Order> getOrderById(Integer id);

    Order saveOrder(Order order);

    Order createOrderWithItem(Order order, Integer productId, Integer quantity);


     //Tạo đơn hàng từ toàn bộ giỏ hàng của user và xóa giỏ hàng sau khi tạo.
    Order createOrderFromCart(String userCode,
                              String recipientName,
                              String recipientPhone,
                              String recipientAddress,
                              String recipientEmail);

    Order updateOrderStatus(Integer id, String status, String cancelReason);

    void deleteOrder(Integer id);

    Page<Order> getOrdersByUser(String userCode, Pageable pageable);
}
