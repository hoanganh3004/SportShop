package com.library.sportshop.controller.user;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @GetMapping("/home")   // khi truy cập localhost:8080/
    public String home() {
        return "user/home";  // Trả về file home.html trong /templates
    }
}
