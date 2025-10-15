package com.library.sportshop.repository;

import com.library.sportshop.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Integer> {

    // Tìm theo tên KH hoặc email
    Page<Order> findByRecipientNameContainingIgnoreCaseOrRecipientEmailContainingIgnoreCase(
            String name, String email, Pageable pageable
    );

    // Lấy đơn hàng của 1 user theo userCode
    Page<Order> findByUserCodeOrderByOrderDateDesc(String userCode, Pageable pageable);
}
