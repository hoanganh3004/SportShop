package com.library.sportshop.controller.user;

import com.library.sportshop.entity.Account;
import com.library.sportshop.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class ProfileController {

    @Autowired
    private AccountService accountService;

    @GetMapping("/profile")
    public String profile(Model model, Principal principal) {
        // Lấy username từ người dùng đang đăng nhập
        String username = principal.getName();

        // Tìm thông tin user theo username
        Account user = accountService.findByUsername(username);

        // Truyền xuống view
        model.addAttribute("user", user);

        return "user/profile";
    }
}
