package com.library.sportshop.config;

import com.library.sportshop.service.AccountService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final AccountService accountService;

    // Sử dụng Constructor Injection thay vì @Autowired trên field
    public SecurityConfig(AccountService accountService) {
        this.accountService = accountService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers(
                                "/blog-detail", "/product-detail", "/product-detail/**", "/blog", "/about", "/contact",
                                "/product", "/login",
                                "/register", "/forgot-password",
                                "/css/**", "/js/**", "/images/**", "/assets/**", "/fonts/**", "/img/**",
                                "/libs/**", "/scss/**", "/tasks/**", "/vendor/**",
                                "/403", "/home")
                        .permitAll()
                        // Các URL cho admin
                        .requestMatchers("/admin", "/admin/**").hasRole("ADMIN")
                        // Các URL cho USER
                        .requestMatchers("/user/**", "/order-history", "/order-history/**", "/profile", "/profile/**",
                                "/checkout", "/cart/**")
                        .hasRole("USER")
                        // Các route còn lại yêu cầu đăng nhập
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .successHandler(customAuthenticationSuccessHandler())
                        .failureHandler((request, response, exception) -> {
                            if (isAccountLocked(exception)) {
                                response.sendRedirect("/login?locked");
                            } else {
                                response.sendRedirect("/login?error");
                            }
                        })
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/home")
                        .permitAll())
                .exceptionHandling(ex -> ex
                        .accessDeniedHandler(accessDeniedHandler()))
                // CSRF disabled for development/testing
                .csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }

    // Tách logic kiểm tra lỗi ra method riêng để code gọn gàng hơn
    private boolean isAccountLocked(Throwable exception) {
        Throwable cause = exception;
        while (cause != null) {
            if (cause instanceof DisabledException || cause instanceof AccountStatusException) {
                return true;
            }
            String msg = cause.getMessage();
            if (msg != null && msg.toLowerCase().contains("disabled")) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return accountService::loadUserByUsername;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public AuthenticationSuccessHandler customAuthenticationSuccessHandler() {
        return (request, response, authentication) -> {
            // 1. Xử lý redirect (ưu tiên tham số 'redirect' nếu có)
            String redirect = request.getParameter("redirect");
            if (redirect != null && !redirect.isBlank() && redirect.startsWith("/")) {
                response.sendRedirect(redirect);
                return;
            }

            // 2. Redirect dựa trên Role
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

            if (isAdmin) {
                response.sendRedirect("/admin");
            } else {
                response.sendRedirect("/home");
            }
        };
    }

    @Bean
    AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.sendRedirect("/403");
        };
    }
}