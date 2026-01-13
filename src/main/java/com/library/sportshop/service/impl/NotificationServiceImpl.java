package com.library.sportshop.service.impl;

import com.library.sportshop.entity.Notification;
import com.library.sportshop.repository.NotificationRepository;
import com.library.sportshop.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Override
    public Page<Notification> getAllNotifications(String keyword, Pageable pageable) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            return notificationRepository.findByUserCodeContainingIgnoreCaseOrMessageContainingIgnoreCase(
                    keyword, keyword, pageable);
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

    // === Methods cho user notifications ===

    @Override
    public long countAll() {
        return notificationRepository.count();
    }

    @Override
    public long countUnreadByUserCode(String userCode) {
        return notificationRepository.countByUserCodeAndIsReadFalse(userCode);
    }

    @Override
    public List<Notification> findTop10ByUserCode(String userCode) {
        return notificationRepository.findTop10ByUserCodeOrderByCreatedAtDesc(userCode);
    }

    @Override
    @Transactional
    public int markAsRead(Integer id, String userCode) {
        return notificationRepository.markAsRead(id, userCode);
    }
}
