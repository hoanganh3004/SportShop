package com.library.sportshop.config;

import com.library.sportshop.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private AccountService accountService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception { http
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers(
                                "/login", "/register", "/forgot-password", "/home", "/css/**", "/js/**", "/images/**", "/assets/**", "/fonts/**", "/img/**", "/libs/**", "/scss/**", "/tasks/**", "/vendor/**").permitAll()
                        // Public access
                        .requestMatchers("/admin/**").hasRole("ADMIN")  // Chỉ ADMIN vào trang admin
                        .requestMatchers("/user/**").hasRole("USER")  // Chỉ USER vào trang user
                        .anyRequest().authenticated()  // Các request khác cần login
                )
                .formLogin(form -> form
                        .loginPage("/login")  // Trang login custom
                        .loginProcessingUrl("/login") // Xử lý POST /login
                        .successHandler(customAuthenticationSuccessHandler())  // Tùy chỉnh redirect dựa trên role
                        .failureUrl("/login?error")  // Redirect khi login thất bại
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")  // URL để logout
                        .logoutSuccessUrl("/home?logout")  // Redirect sau logout
                        .permitAll()
                )
                .csrf(csrf -> csrf.disable())  // Disable CSRF cho test (bật lại production)
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
            String role = authentication.getAuthorities().stream()
                    .filter(auth -> auth.getAuthority().equals("ROLE_ADMIN") || auth.getAuthority().equals("ROLE_USER"))
                    .findFirst()
                    .map(auth -> auth.getAuthority().replace("ROLE_", ""))
                    .orElse("USER");  // Mặc định là USER nếu không xác định

            // Lưu username vào session để view không truy cập thuộc tính null
            request.getSession(true).setAttribute("loggedInUsername", authentication.getName());

            if ("ADMIN".equals(role)) {
                response.sendRedirect("/admin/ad");
            } else {
                response.sendRedirect("/user/home");
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