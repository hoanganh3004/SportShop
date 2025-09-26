package com.library.sportshop.controller.user;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProductController {

    @GetMapping("/product")   // khi truy cập localhost:8080/
    public String product() {
        return "user/product";  // Trả về file home.html trong /templates
    }

    @GetMapping("/product-detail")   // khi truy cập localhost:8080/
    public String productdetail() {
        return "user/product-detail";  // Trả về file home.html trong /templates
    }
}
