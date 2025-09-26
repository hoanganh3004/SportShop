package com.library.sportshop.service;

import com.library.sportshop.entity.Account;
import java.util.List;

public interface AdminAccountService {
    List<Account> getAllAccounts();
    void updateRole(Long id, String role);
    void updateAccount(Long id, Account account);
    void updatePassword(Long id, String newPassword);
    void toggleStatus(Long id);
    Account findByUsername(String username);
}

