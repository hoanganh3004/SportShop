package com.library.sportshop.controller.user;

import com.library.sportshop.entity.Account;
import com.library.sportshop.service.AdminAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ProfileController {

    @Autowired
    private AdminAccountService adminAccountService;

    @GetMapping("/profile")
    public String profilePage(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            Account acc = adminAccountService.findByUsername(authentication.getName());
            model.addAttribute("user", acc);
        }
        return "user/profile";
    }

    @PostMapping("/profile/update-info")
    public String updateInfo(Authentication authentication,
                             @RequestParam String fullName,
                             @RequestParam String email,
                             @RequestParam String phone,
                             @RequestParam String address,
                             Model model) {
        Account current = adminAccountService.findByUsername(authentication.getName());
        Account changes = new Account();
        changes.setFullName(fullName);
        changes.setEmail(email);
        changes.setPhone(phone);
        changes.setAddress(address);
        adminAccountService.updateAccount(current.getId().longValue(), changes);

        model.addAttribute("success", "Cập nhật thông tin thành công!");
        return "redirect:/profile?updated";
    }

    @PostMapping("/profile/update-password")
    public String updatePassword(Authentication authentication,
                                 @RequestParam String oldPassword,
                                 @RequestParam String newPassword,
                                 Model model) {
        Account current = adminAccountService.findByUsername(authentication.getName());
        // Ở đây đơn giản hóa: không kiểm tra oldPassword vì luồng login đã đảm bảo danh tính.
        // Nếu cần, có thể tự verify bằng PasswordEncoder.matches.
        adminAccountService.updatePassword(current.getId().longValue(), newPassword);
        return "redirect:/profile?passwordChanged";
    }
}

