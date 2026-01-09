package com.library.sportshop.controller.admin;

import com.library.sportshop.entity.Account;
import com.library.sportshop.service.AdminAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/adaccount")
public class AdminAccountController {

    @Autowired
    private AdminAccountService adminAccountService;

    // Hiển thị danh sách tài khoản
    @GetMapping
    public String listAccounts(Model model, @RequestParam(name = "keyword", required = false) String keyword) {
        List<Account> accounts;
        if (keyword != null && !keyword.trim().isEmpty()) {
            accounts = adminAccountService.searchAccounts(keyword.trim());
            model.addAttribute("keyword", keyword.trim());
        } else {
            accounts = adminAccountService.getAllAccounts();
        }
        model.addAttribute("accounts", accounts);
        return "admin/adminAccount";
    }

    // Phân quyền tài khoản
    @PostMapping("/updateRole")
    public String updateRole(@RequestParam Long id, @RequestParam String role,
            RedirectAttributes redirectAttributes) {
        try {
            adminAccountService.updateRole(id, role);
            redirectAttributes.addFlashAttribute("success", "Cập nhật vai trò thành công!");
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/adaccount";
    }

    // Cập nhật thông tin
    @PostMapping("/update/{id}")
    public String updateAccount(@PathVariable Long id,
            @ModelAttribute Account account,
            RedirectAttributes redirectAttributes) {
        try {
            adminAccountService.updateAccount(id, account);
            redirectAttributes.addFlashAttribute("success", "Cập nhật thông tin thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Cập nhật thất bại: " + e.getMessage());
        }
        return "redirect:/adaccount";
    }

    // Load form edit (AJAX)
    @GetMapping("/edit/{id}")
    public String editAccountForm(@PathVariable("id") Long id, Model model) {
        Account acc = adminAccountService.getAccountById(id);
        model.addAttribute("account", acc);
        return "admin/editAccount :: editForm";
    }

    // Đổi mật khẩu
    @PostMapping("/password/{id}")
    public String updatePassword(@PathVariable Long id,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            RedirectAttributes redirectAttributes) {
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu nhập lại không khớp!");
            return "redirect:/adaccount";
        }
        adminAccountService.updatePassword(id, newPassword);
        redirectAttributes.addFlashAttribute("success", "Đổi mật khẩu thành công!");
        return "redirect:/adaccount";
    }

    @GetMapping("/change-password/{id}")
    public String changePasswordForm(@PathVariable("id") Long id, Model model) {
        Account acc = adminAccountService.getAccountById(id);
        model.addAttribute("account", acc);
        return "admin/changePassword :: passwordForm"; // fragment trong changePassword.html
    }

    // Khóa/Mở tài khoản
    @GetMapping("/toggle/{id}")
    public String toggleStatus(@PathVariable Long id, Model model) {
        try {
            adminAccountService.toggleStatus(id);
            return "redirect:/adaccount?success";
        } catch (IllegalStateException ex) {
            model.addAttribute("error", ex.getMessage());
            List<Account> accounts = adminAccountService.getAllAccounts();
            model.addAttribute("accounts", accounts);
            return "admin/adminAccount";
        }
    }
}
