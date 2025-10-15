package com.library.sportshop.controller.user;

import com.library.sportshop.entity.Account;
import com.library.sportshop.entity.Notification;
import com.library.sportshop.repository.NotificationRepository;
import com.library.sportshop.service.AccountService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;
import java.util.List;

@ControllerAdvice
public class UserNotificationAdvice {

    @Autowired
    private AccountService accountService;

    @Autowired
    private NotificationRepository notificationRepository;

    @ModelAttribute
    public void loadUserNotifications(Principal principal, HttpSession session) {
        try {
            if (principal == null) return;
            Account acc = accountService.findByUsername(principal.getName());
            if (acc == null) return;
            String userCode = acc.getCode();
            List<Notification> items = notificationRepository.findTop10ByUserCodeOrderByCreatedAtDesc(userCode);
            long unread = notificationRepository.countByUserCodeAndIsReadFalse(userCode);
            session.setAttribute("notifications", items);
            session.setAttribute("notificationCount", unread);
        } catch (Exception ignored) {}
    }
}
