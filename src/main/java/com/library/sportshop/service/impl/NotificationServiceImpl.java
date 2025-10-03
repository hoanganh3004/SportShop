package com.library.sportshop.service.impl;

import com.library.sportshop.entity.Notification;
import com.library.sportshop.repository.NotificationRepository;
import com.library.sportshop.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Override
    public Page<Notification> getAllNotifications(String keyword, Pageable pageable) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            return notificationRepository.findByUserCodeContainingIgnoreCaseOrMessageContainingIgnoreCase(
                    keyword, keyword, pageable
            );
        }
        return notificationRepository.findAll(pageable);
    }

    @Override
    public Optional<Notification> getNotificationById(Integer id) {
        return notificationRepository.findById(id);
    }

    @Override
    public Notification saveNotification(Notification notification) {
        if (notification.getCreatedAt() == null) {
            notification.setCreatedAt(LocalDateTime.now());
        }
        if (notification.getRead() == null) {
            notification.setRead(false);
        }
        return notificationRepository.save(notification);
    }
}
