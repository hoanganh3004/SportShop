package com.library.sportshop.controller.user;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ContactController {
    @GetMapping("/contact")   // khi truy cập localhost:8080/
    public String contact() {
        return "user/contact";  // Trả về file home.html trong /templates
    }
}
