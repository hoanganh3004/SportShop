package com.library.sportshop.repository;

import com.library.sportshop.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    // Tìm kiếm theo userCode hoặc message
    Page<Notification> findByUserCodeContainingIgnoreCaseOrMessageContainingIgnoreCase(
            String userCode, String message, Pageable pageable
    );

    // Lấy 10 thông báo mới nhất của user
    java.util.List<Notification> findTop10ByUserCodeOrderByCreatedAtDesc(String userCode);

    // Đếm thông báo chưa đọc
    long countByUserCodeAndIsReadFalse(String userCode);

    // Đánh dấu đã đọc (trả về số bản ghi bị ảnh hưởng)
    @Modifying
    @Transactional
    @Query("update Notification n set n.isRead = true where n.id = :id and n.userCode = :userCode and (n.isRead = false or n.isRead is null)")
    int markAsRead(@Param("id") Integer id, @Param("userCode") String userCode);
}
