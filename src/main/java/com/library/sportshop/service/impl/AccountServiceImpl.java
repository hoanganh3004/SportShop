package com.library.sportshop.service.impl;

import com.library.sportshop.entity.Account;
import com.library.sportshop.repository.AccountRepository;
import com.library.sportshop.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class AccountServiceImpl implements AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]+$");

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        if (Boolean.FALSE.equals(account.getStatus())) {
            throw new UsernameNotFoundException("Account is disabled: " + username);
        }

        String role = "ROLE_" + account.getRole().name();
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role);

        return org.springframework.security.core.userdetails.User.builder()
                .username(account.getUsername())
                .password(account.getPassword())
                .authorities(authority)
                .disabled(Boolean.FALSE.equals(account.getStatus()))
                .build();
    }

    @Override
    public boolean registerAccount(Account account) {
        // Check username format
        if (!USERNAME_PATTERN.matcher(account.getUsername()).matches()) {
            throw new IllegalArgumentException("Username chỉ được chứa chữ, số và dấu gạch dưới (_)");
        }

        // Check username exists
        if (accountRepository.findByUsername(account.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username đã tồn tại");
        }

        // Check email exists
        if (accountRepository.findByEmail(account.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email đã tồn tại");
        }

        // Set code nếu chưa có
        if (account.getCode() == null) {
            account.setCode(UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }

        // Encode password
        account.setPassword(passwordEncoder.encode(account.getPassword()));

        accountRepository.save(account);
        return true;
    }

    @Override
    public boolean resetPassword(String username, String code, String newPassword) {
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        if (!account.getCode().equals(code)) {
            return false;
        }

        account.setPassword(passwordEncoder.encode(newPassword));
        accountRepository.save(account);
        return true;
    }
}
