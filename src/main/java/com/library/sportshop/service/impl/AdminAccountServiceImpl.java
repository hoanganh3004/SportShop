package com.library.sportshop.service.impl;

import com.library.sportshop.entity.Account;
import com.library.sportshop.repository.AccountRepository;
import com.library.sportshop.service.AdminAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminAccountServiceImpl implements AdminAccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; // dùng khi đổi mật khẩu

    @Override
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    @Override
    public List<Account> searchAccounts(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return accountRepository.findAll();
        }
        return accountRepository
                .findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrFullNameContainingIgnoreCase(keyword,
                        keyword, keyword);
    }

    @Override
    public void updateRole(Long id, String role) {
        Account acc = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        String currentUsername = getCurrentUsername();
        if (currentUsername != null && currentUsername.equals(acc.getUsername())) {
            throw new IllegalStateException("Không thể tự thay đổi vai trò của chính mình");
        }
        try {
            acc.setRole(Account.Role.valueOf(role.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid role: " + role);
        }
        accountRepository.save(acc);
    }

    @Override
    public void toggleStatus(Long id) {
        Account acc = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        String currentUsername = getCurrentUsername();
        if (currentUsername != null && currentUsername.equals(acc.getUsername())) {
            throw new IllegalStateException("Không thể tự khóa tài khoản của chính mình");
        }
        acc.setStatus(!acc.getStatus()); // true -> false, false -> true
        accountRepository.save(acc);
    }

    @Override
    public Account findByUsername(String username) {
        return accountRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Account not found"));
    }

    @Override
    public void updatePassword(Long id, String newPassword) {
        Account acc = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        acc.setPassword(passwordEncoder.encode(newPassword));
        accountRepository.save(acc);
    }

    @Override
    public void updateAccount(Long id, Account account) {
        Account acc = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        // cập nhật các thông tin cơ bản (trừ password/role/status)
        acc.setFullName(account.getFullName());
        acc.setEmail(account.getEmail());
        acc.setPhone(account.getPhone());
        acc.setAddress(account.getAddress());

        accountRepository.save(acc);
    }

    @Override
    public Account getAccountById(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (authentication != null) ? authentication.getName() : null;
    }

    @Override
    public long countAccounts() {
        return accountRepository.count();
    }
}
