package com.library.sportshop.service.impl;

import com.library.sportshop.entity.Account;
import com.library.sportshop.repository.AccountRepository;
import com.library.sportshop.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.mail.javamail.JavaMailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.MimeMessageHelper;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.UUID;
import java.util.regex.Pattern;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;

@Service
public class AccountServiceImpl implements AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountServiceImpl.class);

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String mailFrom;

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]+$");

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        if (Boolean.FALSE.equals(account.getStatus())) {
            throw new DisabledException("Account is disabled: " + username);
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
        // Kiểm tra định dạng tên người dùng
        if (!USERNAME_PATTERN.matcher(account.getUsername()).matches()) {
            throw new IllegalArgumentException("Username chỉ được chứa chữ, số và dấu gạch dưới (_)");
        }

        // Kiểm tra tên người dùng có tồn tại không
        if (accountRepository.findByUsername(account.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username đã tồn tại");
        }

        // Kiểm tra email có tồn tại không
        if (accountRepository.findByEmail(account.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email đã tồn tại");
        }

        // Set code
        if (account.getCode() == null) {
            account.setCode(UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }

        // Mã hóa mật khẩu
        account.setPassword(passwordEncoder.encode(account.getPassword()));
        // set trạng thái hoạt động cho người dùng mới đăng ký
        account.setStatus(true);

        accountRepository.save(account);
        return true;
    }

    @Override
    public boolean resetPasswordByEmail(String email) {
        var opt = accountRepository.findByEmail(email);
        if (opt.isEmpty()) {
            return false;
        }
        Account account = opt.get();
        // generate random 8-char password including letters, digits, and special characters
        String newPass = generatePassword(8);

        // hash and save
        account.setPassword(passwordEncoder.encode(newPass));
        accountRepository.save(account);

        // send email with sender name "SportShop"
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            helper.setTo(email);
            helper.setSubject("Khôi phục mật khẩu - SportShop");
            helper.setText("Mật khẩu mới của bạn là: " + newPass + "\nVui lòng đăng nhập và đổi mật khẩu ngay sau khi vào hệ thống.", false);
            if (mailFrom != null && !mailFrom.isBlank()) {
                helper.setFrom(new InternetAddress(mailFrom, "SportShop"));
            }
            mailSender.send(mimeMessage);
            log.info("[mail] Sent reset password email to {}", email);
        } catch (Exception e) {
            log.error("[mail] Failed to send reset password email to {}", email, e);
        }
        return true;
    }

    private String generatePassword(int length) {
        final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        final String LOWER = "abcdefghijklmnopqrstuvwxyz";
        final String DIGITS = "0123456789";
        final String SPECIAL = "!@#$%^&*()-_=+[]{};:,.?/";
        final String ALL = UPPER + LOWER + DIGITS + SPECIAL;

        SecureRandom rnd = new SecureRandom();
        ArrayList<Character> chars = new ArrayList<>();
        // Ensure at least one from each category
        chars.add(UPPER.charAt(rnd.nextInt(UPPER.length())));
        chars.add(LOWER.charAt(rnd.nextInt(LOWER.length())));
        chars.add(DIGITS.charAt(rnd.nextInt(DIGITS.length())));
        chars.add(SPECIAL.charAt(rnd.nextInt(SPECIAL.length())));

        for (int i = chars.size(); i < length; i++) {
            chars.add(ALL.charAt(rnd.nextInt(ALL.length())));
        }
        Collections.shuffle(chars, rnd);

        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) sb.append(chars.get(i));
        return sb.toString();
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

    @Override
    public Account findByUsername(String username) {
        return accountRepository.findByUsername(username).orElse(null);
    }

}
