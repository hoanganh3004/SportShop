package com.library.sportshop.service;

import com.library.sportshop.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface NotificationService {
    Page<Notification> getAllNotifications(String keyword, Pageable pageable);

    Optional<Notification> getNotificationById(Integer id);

    Notification saveNotification(Notification notification);
}
