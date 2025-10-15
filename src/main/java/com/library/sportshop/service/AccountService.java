package com.library.sportshop.service;

import com.library.sportshop.entity.Account;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface AccountService {
    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;
    boolean registerAccount(Account account);  // Sử dụng Account object
    boolean resetPassword(String username, String code, String newPassword);
    Account findByUsername(String username);
    boolean resetPasswordByEmail(String email);
}