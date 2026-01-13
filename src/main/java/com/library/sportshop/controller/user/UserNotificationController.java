package com.library.sportshop.controller.user;

import com.library.sportshop.entity.Account;
import com.library.sportshop.entity.Notification;
import com.library.sportshop.service.AccountService;
import com.library.sportshop.service.NotificationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@ControllerAdvice
public class UserNotificationController {

    private static final Logger log = LoggerFactory.getLogger(UserNotificationController.class);

    @Autowired
    private AccountService accountService;

    @Autowired
    private NotificationService notificationService;

    /**
     * Đánh dấu thông báo là đã đọc.
     * Tương thích với nhiều đường dẫn khác nhau để tránh bị chặn bởi AdBlock:
     * - /user/notifications/read (JS chính)
     * - /notifications/read
     * - /user/noti/read
     * - /noti/read
     * - /nr (rút gọn, an toàn nhất)
     *
     * Trả về: số lượng thông báo chưa đọc còn lại (dưới dạng chuỗi số)
     */
    @PostMapping({
            "/user/notifications/read",
            "/notifications/read",
            "/user/noti/read",
            "/noti/read",
            "/nr"
    })
    public String markNotificationAsRead(@RequestParam("id") Integer id,
            HttpSession session,
            HttpServletRequest request,
            HttpServletResponse response,
            java.security.Principal principal) {
        try {
            // Ưu tiên lấy userCode từ session (đã được set ở AuthController)
            String userCode = (String) session.getAttribute("loggedInUsername");

            // Nếu session không có, lấy từ Principal (Spring Security)
            if ((userCode == null || userCode.isEmpty()) && principal != null) {
                Account acc = accountService.findByUsername(principal.getName());
                if (acc != null) {
                    userCode = acc.getCode();
                    // Đồng thời set vào session để lần sau không phải query lại
                    session.setAttribute("loggedInUsername", userCode);
                }
            }

            if (userCode == null || userCode.isEmpty()) {
                log.warn("[notify] Không có userCode cho id={} principal={}", id,
                        principal != null ? principal.getName() : "null");
                return "0";
            }

            log.info("[notify] Đánh dấu đã đọc id={} userCode={}", id, userCode);

            // Cập nhật trạng thái thông báo trong DB thông qua service
            int affected = notificationService.markAsRead(id, userCode);
            log.info("[notify] markAsRead id={} userCode={} affectedRows={}", id, userCode, affected);

            // Đếm lại số thông báo chưa đọc thông qua service
            long unread = notificationService.countUnreadByUserCode(userCode);
            log.info("[notify] unread còn lại userCode={} => {}", userCode, unread);

            // Cập nhật session để hiển thị badge đúng
            session.setAttribute("notificationCount", unread);

            // Nếu client mong muốn text/plain (JS fetch của header), trả về số.
            String accept = request != null ? request.getHeader("Accept") : null;
            if (accept != null && accept.contains("text/plain")) {
                return String.valueOf(unread);
            }
            // Nếu là form submit (không phải fetch text/plain), redirect về trang trước để
            // tránh trang trắng và lỗi favicon.
            String referer = request != null ? request.getHeader("Referer") : null;
            if (referer == null || referer.isEmpty())
                referer = "/home";
            try {
                if (response != null) {
                    response.sendRedirect(referer);
                    return "";
                }
            } catch (Exception ignored) {
            }
            return String.valueOf(unread);
        } catch (Exception e) {
            log.error("[notify] Lỗi khi cập nhật trạng thái id={} err={}", id, e.getMessage(), e);
            return "0";
        }
    }

    /**
     * Fallback GET - dành cho môi trường chặn POST hoặc AdBlock
     */
    @GetMapping({
            "/user/noti/read",
            "/noti/read",
            "/nr"
    })
    public String markNotificationAsReadGet(@RequestParam("id") Integer id,
            HttpSession session,
            HttpServletRequest request,
            HttpServletResponse response,
            java.security.Principal principal) {
        return markNotificationAsRead(id, session, request, response, principal);
    }

    @ModelAttribute
    public void loadUserNotifications(Principal principal, HttpSession session) {
        try {
            if (principal == null)
                return;
            Account acc = accountService.findByUsername(principal.getName());
            if (acc == null)
                return;
            String userCode = acc.getCode();
            // Sử dụng service thay vì repository trực tiếp
            List<Notification> items = notificationService.findTop10ByUserCode(userCode);
            long unread = notificationService.countUnreadByUserCode(userCode);
            session.setAttribute("notifications", items);
            session.setAttribute("notificationCount", unread);
        } catch (Exception ignored) {
        }
    }

    // Mở thông báo: đánh dấu là đã đọc, sau đó thử chuyển hướng đến chi tiết đơn
    // hàng nếu tin nhắn chứa mã đơn hàng
    @GetMapping({
            "/notifications/open/{id}",
            "/no/{id}"
    })
    public void openNotification(@PathVariable("id") Integer id,
            HttpSession session,
            HttpServletRequest request,
            HttpServletResponse response,
            java.security.Principal principal) {
        try {
            // Lấy userCode từ session hoặc Principal
            String userCode = (String) session.getAttribute("loggedInUsername");
            if ((userCode == null || userCode.isEmpty()) && principal != null) {
                Account acc = accountService.findByUsername(principal.getName());
                if (acc != null) {
                    userCode = acc.getCode();
                    session.setAttribute("loggedInUsername", userCode);
                }
            }

            if (userCode != null && !userCode.isEmpty()) {
                // mark read (id + userCode) thông qua service
                int affected = notificationService.markAsRead(id, userCode);
                log.info("[notify] openNotification marked as read id={} userCode={} affected={}", id, userCode,
                        affected);
            } else {
                log.warn("[notify] openNotification no userCode for id={}", id);
            }

            // phân tích cú pháp ID đơn hàng từ tin nhắn thông báo
            Integer orderId = null;
            try {
                // Sử dụng service thay vì repository trực tiếp
                Optional<Notification> opt = notificationService.getNotificationById(id);
                if (opt.isPresent()) {
                    String msg = String.valueOf(opt.get().getMessage());
                    // Regex 1: "Mã đơn #<number>"
                    Pattern p1 = Pattern.compile("(?i)m[aã] ?đơn\\s*#\\s*(\\d+)");
                    Matcher m1 = p1.matcher(msg);
                    if (m1.find()) {
                        orderId = Integer.valueOf(m1.group(1));
                    } else {
                        // Regex 2: last "#<number>" fallback
                        Pattern p2 = Pattern.compile("#\\s*(\\d+)");
                        Matcher m2 = p2.matcher(msg);
                        Integer last = null;
                        while (m2.find()) {
                            last = Integer.valueOf(m2.group(1));
                        }
                        orderId = last;
                    }
                }
            } catch (Exception ignored) {
            }

            String ctx = request != null ? request.getContextPath() : "";
            if (ctx == null)
                ctx = "";
            String target = ctx + "/order-history" + (orderId != null ? ("?open=" + orderId) : "");
            if (response != null) {
                response.sendRedirect(target);
            }
        } catch (Exception ignored) {
            try {
                if (response != null)
                    response.sendRedirect("/order-history");
            } catch (Exception __) {
            }
        }
    }
}
