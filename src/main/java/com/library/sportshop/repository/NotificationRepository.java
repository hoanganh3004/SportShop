package com.library.sportshop.repository;

import com.library.sportshop.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    // Tìm kiếm theo userCode hoặc message
    Page<Notification> findByUserCodeContainingIgnoreCaseOrMessageContainingIgnoreCase(
            String userCode, String message, Pageable pageable
    );

    // Lấy 10 thông báo mới nhất của user
    java.util.List<Notification> findTop10ByUserCodeOrderByCreatedAtDesc(String userCode);

    // Đếm thông báo chưa đọc
    long countByUserCodeAndIsReadFalse(String userCode);
}
