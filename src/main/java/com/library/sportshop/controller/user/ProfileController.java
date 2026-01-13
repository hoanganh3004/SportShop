package com.library.sportshop.controller.user;

import com.library.sportshop.entity.Account;
import com.library.sportshop.service.AdminAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.regex.Pattern;

@Controller
public class ProfileController {

    @Autowired
    private AdminAccountService adminAccountService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Regex patterns cho validation
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{10,11}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@gmail\\.com$");

    @GetMapping("/profile")
    public String profilePage(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            Account acc = adminAccountService.findByUsername(authentication.getName());
            model.addAttribute("user", acc);
        }
        return "user/profile";
    }

    @GetMapping("/profile/change-password")
    public String changePasswordPage(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        Account acc = adminAccountService.findByUsername(authentication.getName());
        model.addAttribute("user", acc);
        return "user/changePassword";
    }

    @PostMapping("/profile/update-info")
    public String updateInfo(Authentication authentication,
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam String phone,
            @RequestParam String address,
            RedirectAttributes redirectAttributes) {
        // Validation: fullName không rỗng
        if (fullName == null || fullName.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Họ tên không được để trống");
            return "redirect:/profile";
        }

        // Validation: email phải có @gmail.com
        if (email == null || !EMAIL_PATTERN.matcher(email.trim()).matches()) {
            redirectAttributes.addFlashAttribute("error",
                    "Email phải có định dạng @gmail.com (ví dụ: example@gmail.com)");
            return "redirect:/profile";
        }

        // Validation: phone chỉ chứa số và 10-11 ký tự
        if (phone == null || !PHONE_PATTERN.matcher(phone.trim()).matches()) {
            redirectAttributes.addFlashAttribute("error",
                    "Số điện thoại phải là 10-11 chữ số, không chứa chữ hoặc ký tự đặc biệt");
            return "redirect:/profile";
        }

        // Validation: address không rỗng
        if (address == null || address.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Địa chỉ không được để trống");
            return "redirect:/profile";
        }

        Account current = adminAccountService.findByUsername(authentication.getName());
        Account changes = new Account();
        changes.setFullName(fullName.trim());
        changes.setEmail(email.trim());
        changes.setPhone(phone.trim());
        changes.setAddress(address.trim());
        adminAccountService.updateAccount(current.getId().longValue(), changes);

        return "redirect:/profile?updated";
    }

    @PostMapping("/profile/update-password")
    public String updatePassword(Authentication authentication,
            @RequestParam String oldPassword,
            @RequestParam String newPassword,
            @RequestParam(required = false) String confirmPassword,
            RedirectAttributes redirectAttributes) {
        Account current = adminAccountService.findByUsername(authentication.getName());
        if (current == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy tài khoản");
            return "redirect:/profile/change-password";
        }

        // Verify old password
        if (!passwordEncoder.matches(oldPassword, current.getPassword())) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu cũ không đúng");
            return "redirect:/profile/change-password";
        }

        // Validate new password
        if (newPassword == null || newPassword.trim().length() < 6) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu mới phải có ít nhất 6 ký tự");
            return "redirect:/profile/change-password";
        }

        // Validate confirm password
        if (confirmPassword != null && !confirmPassword.equals(newPassword)) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu xác nhận không khớp");
            return "redirect:/profile/change-password";
        }

        // Check new password is different from old
        if (passwordEncoder.matches(newPassword, current.getPassword())) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu mới phải khác mật khẩu hiện tại");
            return "redirect:/profile/change-password";
        }

        adminAccountService.updatePassword(current.getId().longValue(), newPassword);
        return "redirect:/profile?passwordChanged";
    }
}
