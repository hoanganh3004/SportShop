package com.library.sportshop.controller.user;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BlogController {
    @GetMapping("/blog")   // khi truy cập localhost:8080/
    public String blog() {
        return "user/blog";  // Trả về file home.html trong /templates
    }

    @GetMapping("/blog-detail")   // khi truy cập localhost:8080/
    public String blogdetail() {
        return "user/blog-detail";  // Trả về file home.html trong /templates
    }

    @GetMapping("/about")   // khi truy cập localhost:8080/
    public String about() {
        return "user/about";  // Trả về file home.html trong /templates
    }
}
