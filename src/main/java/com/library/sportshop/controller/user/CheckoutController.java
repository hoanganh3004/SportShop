package com.library.sportshop.controller.user;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CheckoutController {
    @GetMapping("/checkout")   // khi truy cập localhost:8080/
    public String checkout() {
        return "user/checkout";  // Trả về file home.html trong /templates
    }
}
