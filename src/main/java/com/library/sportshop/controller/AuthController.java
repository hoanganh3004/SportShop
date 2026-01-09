package com.library.sportshop.controller;

import com.library.sportshop.entity.Account;
import com.library.sportshop.service.AccountService;
import com.library.sportshop.repository.AccountRepository;
import com.library.sportshop.repository.OrderRepository;
import com.library.sportshop.repository.ProductRepository;
import com.library.sportshop.repository.NotificationRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AccountService accountService;

    @Autowired
    private com.library.sportshop.repository.CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @GetMapping("/login")
    public String login(Model model) {
        return "acc/login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("account", new Account());
        return "acc/register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute("account") @Valid Account account,
            BindingResult bindingResult,
            @RequestParam("confirmPassword") String confirmPassword,
            Model model) {

        // Validate các field bắt buộc
        if (bindingResult.hasErrors()) {
            return "acc/register";
        }

        // Check confirm password
        if (!account.getPassword().equals(confirmPassword)) {
            model.addAttribute("errorMessage", "Mật khẩu xác nhận không khớp");
            return "acc/register";
        }

        try {
            accountService.registerAccount(account);
            model.addAttribute("successMessage", "Đăng ký thành công. Vui lòng đăng nhập.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "acc/register";
        } catch (Exception e) {
            log.error("Lỗi đăng ký tài khoản", e);
            model.addAttribute("errorMessage", "Lỗi hệ thống: " + e.getMessage());
            return "acc/register";
        }
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordForm(Model model) {
        model.addAttribute("resetDto", new Object());
        return "acc/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPasswordSubmit(@RequestParam("email") String email,
            Model model) {
        boolean success = accountService.resetPasswordByEmail(email);
        if (success) {
            model.addAttribute("success",
                    "Mật khẩu mới đã được gửi về email của bạn. Vui lòng kiểm tra hộp thư (hoặc spam)");
        } else {
            model.addAttribute("error", "Email không tồn tại trong hệ thống");
        }
        return "acc/forgot-password";
    }

    @GetMapping("/admin/ad")
    public String adminDashboard(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            model.addAttribute("username", authentication.getName());
        }
        model.addAttribute("totalProducts", productRepository.count());
        model.addAttribute("totalOrders", orderRepository.count());
        model.addAttribute("totalUsers", accountRepository.count());
        model.addAttribute("totalNotifications", notificationRepository.count());
        return "admin/ad";
    }

    @GetMapping("/user/home")
    public String userHome(Model model, Authentication authentication, HttpSession session) {
        if (authentication != null && authentication.isAuthenticated()) {
            model.addAttribute("username", authentication.getName());
            Account account = accountService.findByUsername(authentication.getName());
            if (account != null) {
                session.setAttribute("loggedInUsername", account.getCode());
                Integer totalQty = cartItemRepository.countQuantityByUserCode(account.getCode());
                session.setAttribute("cartQty", totalQty == null ? 0 : totalQty);
            }
        }
        return "user/home";
    }
}