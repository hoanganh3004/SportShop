package com.library.sportshop.controller.user;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;

import com.library.sportshop.entity.Account;
import com.library.sportshop.entity.Notification;
import com.library.sportshop.dto.CartItemResponseDTO;
import com.library.sportshop.service.AccountService;
import com.library.sportshop.service.CartService;
import com.library.sportshop.service.OrderService;
import com.library.sportshop.service.NotificationService;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
public class CheckoutController {
    @Autowired
    private AccountService accountService;

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/checkout")
    // trang checkout: hiển thị giỏ hàng và tổng tiền
    public String checkout(Model model, Principal principal, HttpSession session) {
        // yêu cầu đăng nhập
        if (principal == null) {
            return "redirect:/login";
        }

        Account account = accountService.findByUsername(principal.getName());
        if (account == null) {
            return "redirect:/login";
        }

        // nạp giỏ hàng từ DB
        String userCode = account.getCode();
        var items = cartService.findByUserCode(userCode);
        List<CartItemResponseDTO> cartItems = cartService.mapToDto(items);

        // tính tổng số lượng và tiền
        int totalQty = cartService.countQuantityByUserCode(userCode);
        session.setAttribute("cartQty", totalQty);

        long subtotal = 0L;
        for (CartItemResponseDTO it : cartItems) {
            if (it.getProduct() != null) {
                long price = it.getProduct().getPrice() == null ? 0L : it.getProduct().getPrice().longValue();
                int qty = it.getQuantity() == null ? 0 : it.getQuantity();
                subtotal += price * qty;
            }
        }

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("total", subtotal); // chưa có phí ship/giảm giá -> total = subtotal
        model.addAttribute("account", account);

        return "user/checkout";
    }

    @PostMapping("/checkout")
    // đặt hàng toàn bộ giỏ hiện tại
    public String placeOrder(@RequestParam("fullname") String fullname,
                             @RequestParam("phone") String phone,
                             @RequestParam("address") String address,
                             Principal principal,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/login";
        }
        Account account = accountService.findByUsername(principal.getName());
        if (account == null) {
            return "redirect:/login";
        }

        try {
            // tạo đơn từ toàn bộ giỏ hàng
            var order = orderService.createOrderFromCart(
                    account.getCode(),
                    fullname,
                    phone,
                    address,
                    account.getEmail()
            );

            // gửi thông báo vào DB cho người dùng
            try {
                Notification userNoti = new Notification();
                userNoti.setUserCode(account.getCode());
                userNoti.setMessage("Bạn đã đặt hàng thành công. Mã đơn #" + order.getId());
                notificationService.saveNotification(userNoti);
            } catch (Exception ex) {
                // bỏ qua lỗi thông báo để không ảnh hưởng trải nghiệm đặt hàng
            }

            // đồng bộ badge giỏ hàng về 0
            session.setAttribute("cartQty", 0);

            // đặt flash message và chuyển về trang chủ
            redirectAttributes.addFlashAttribute("successMessage", "Đặt hàng thành công! Cảm ơn bạn đã mua sắm.");
            return "redirect:/home";
        } catch (IllegalStateException ex) {
            // hết hàng hoặc không đủ số lượng: trở lại trang checkout với thông báo
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/checkout";
        }
    }
}
