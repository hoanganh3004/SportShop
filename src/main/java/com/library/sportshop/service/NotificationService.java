package com.library.sportshop.service;

import com.library.sportshop.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface NotificationService {
    Page<Notification> getAllNotifications(String keyword, Pageable pageable);

    Optional<Notification> getNotificationById(Integer id);

    Notification saveNotification(Notification notification);

    // === Methods cho user notifications ===

    // Đếm tổng số thông báo
    long countAll();

    // Đếm thông báo chưa đọc của user
    long countUnreadByUserCode(String userCode);

    // Lấy 10 thông báo mới nhất của user
    List<Notification> findTop10ByUserCode(String userCode);

    // Đánh dấu thông báo đã đọc
    int markAsRead(Integer id, String userCode);
}
