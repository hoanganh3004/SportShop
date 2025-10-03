package com.library.sportshop.controller.admin;

import com.library.sportshop.entity.Notification;
import com.library.sportshop.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/adnotification")
public class AdminNotificationController {

    @Autowired
    private NotificationService notificationService;

    // Hiển thị danh sách + tìm kiếm
    @GetMapping
    public String listNotifications(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) String keyword,
            Model model
    ) {
        int pageSize = 10;
        Page<Notification> notificationPage =
                notificationService.getAllNotifications(keyword, PageRequest.of(page - 1, pageSize));

        model.addAttribute("notifications", notificationPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", notificationPage.getTotalPages());
        model.addAttribute("keyword", keyword);

        return "admin/adminNotification";
    }

    // Load form tạo thông báo (AJAX)
    @GetMapping("/new")
    public String newNotificationForm(Model model) {
        model.addAttribute("notification", new Notification());
        return "admin/addNotification";
    }

    // Lưu thông báo
    @PostMapping("/save")
    public String saveNotification(@ModelAttribute Notification notification, Model model) {
        try {
            notificationService.saveNotification(notification);
            model.addAttribute("success", "Tạo thông báo thành công!");
        } catch (Exception e) {
            model.addAttribute("error", "Không thể tạo thông báo!");
        }
        return "redirect:/adnotification";
    }

    // Xem chi tiết (AJAX)
    @GetMapping("/detail/{id}")
    public String detailNotification(@PathVariable Integer id, Model model) {
        Optional<Notification> notification = notificationService.getNotificationById(id);
        if (notification.isPresent()) {
            model.addAttribute("notification", notification.get());
        } else {
            model.addAttribute("error", "Không tìm thấy thông báo");
        }
        return "admin/notificationDetail";
    }
}
