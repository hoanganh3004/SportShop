package com.library.sportshop.controller.admin;

import com.library.sportshop.entity.Account;
import com.library.sportshop.service.AdminAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/adaccount")
public class AdminAccountController {

    @Autowired
    private AdminAccountService adminAccountService;

    //  Hiển thị danh sách tài khoản
    @GetMapping
    public String listAccounts(Model model) {
        List<Account> accounts = adminAccountService.getAllAccounts();
        model.addAttribute("accounts", accounts);
        return "admin/adminAccount";
    }

    // Phân quyền tài khoản
    @PostMapping("/updateRole")
    public String updateRole(@RequestParam Long id, @RequestParam String role, Model model) {
        try {
            adminAccountService.updateRole(id, role);
            return "redirect:/adaccount?success";
        } catch (IllegalStateException ex) {
            model.addAttribute("error", ex.getMessage());
            List<Account> accounts = adminAccountService.getAllAccounts();
            model.addAttribute("accounts", accounts);
            return "admin/adminAccount";
        }
    }

    //  Cập nhật thông tin
    @PostMapping("/update/{id}")
    public String updateAccount(@PathVariable Long id,
                                @ModelAttribute Account account) {
        adminAccountService.updateAccount(id, account);
        return "redirect:/adaccount";
    }

    //  Đổi mật khẩu
    @PostMapping("/password/{id}")
    public String updatePassword(@PathVariable Long id,
                                 @RequestParam String newPassword) {
        adminAccountService.updatePassword(id, newPassword);
        return "redirect:/adaccount";
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
