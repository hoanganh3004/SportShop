package com.library.sportshop.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdController {
    @GetMapping("/admin")   // khi truy cập localhost:8080/
    public String ad() {
        return "admin/ad";  // Trả về file home.html trong /templates
    }
}
