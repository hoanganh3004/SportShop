package com.library.sportshop.config;

import com.library.sportshop.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private AccountService accountService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers(
                                "/blog-detail", "/product-detail", "/product-detail/**", "/blog", "/about", "/contact", "/product", "/login",
                                "/register", "/forgot-password",
                                "/css/**", "/js/**", "/images/**", "/assets/**", "/fonts/**", "/img/**",
                                "/libs/**", "/scss/**", "/tasks/**", "/vendor/**",
                                "/403"
                        ).permitAll()

                        // Trang home: cho phép ANONYMOUS hoặc USER hoặc ADMIN
                        .requestMatchers("/home")
                        .access(new WebExpressionAuthorizationManager("isAnonymous() or hasRole('USER') or hasRole('ADMIN')"))

                        // Các URL cho admin
                        .requestMatchers("/admin", "/admin/**").hasRole("ADMIN")

                        // Các URL cho USER
                        .requestMatchers("/user/**").hasRole("USER")

                        // Các route còn lại yêu cầu đăng nhập
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .successHandler(customAuthenticationSuccessHandler())
                        .failureHandler((request, response, exception) -> {
                            Throwable cursor = exception;
                            boolean isLocked = false;
                            while (cursor != null && !isLocked) {
                                if (cursor instanceof DisabledException || cursor instanceof AccountStatusException) {
                                    isLocked = true;
                                    break;
                                }
                                String msg = cursor.getMessage();
                                if (msg != null && msg.toLowerCase().contains("disabled")) {
                                    isLocked = true;
                                    break;
                                }
                                cursor = cursor.getCause();
                            }
                            response.sendRedirect(isLocked ? "/login?locked" : "/login?error");
                        })
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/home")
                        .permitAll()
                )
                .exceptionHandling(ex -> ex
                        .accessDeniedHandler(accessDeniedHandler())
                )
                .csrf(csrf -> csrf.disable())
                .authenticationProvider(authenticationProvider(userDetailsService()));

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> accountService.loadUserByUsername(username);
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(UserDetailsService userDetailsService) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public AuthenticationSuccessHandler customAuthenticationSuccessHandler() {
        return (request, response, authentication) -> {
            // Ưu tiên redirect về trang trước đó nếu có tham số 'redirect'
            try {
                String redirect = request.getParameter("redirect");
                if (redirect != null && !redirect.isBlank()) {
                    // Chỉ cho phép đường dẫn nội bộ để tránh open-redirect
                    if (!redirect.startsWith("/")) {
                        redirect = "/";
                    }
                    response.sendRedirect(redirect);
                    return;
                }
            } catch (Exception ignored) {}
            // Kiểm tra role cụ thể
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

            boolean isUser = authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER"));

            // Lưu username và role vào session
            request.getSession(true).setAttribute("loggedInUsername", authentication.getName());
            request.getSession(true).setAttribute("userRole", isAdmin ? "ADMIN" : "USER");

            if (isAdmin) {
                response.sendRedirect("/admin");
            } else if (isUser) {
                response.sendRedirect("/home");
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