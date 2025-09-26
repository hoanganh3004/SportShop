package com.library.sportshop.controller.user;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class OderHistoryController {
    @GetMapping("/oderhistory")   // khi truy cập localhost:8080/
    public String oderhistory() {
        return "user/oderHistory";  // Trả về file home.html trong /templates
    }
}
