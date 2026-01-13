package com.library.sportshop.controller.user;

import com.library.sportshop.entity.Product;
import com.library.sportshop.service.AdminProductService;
import com.library.sportshop.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private AdminProductService adminProductService;

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/home") // khi truy cập localhost:8080/
    public String home(Model model,
            @RequestParam(value = "q", required = false) String q) {
        Pageable topEight = PageRequest.of(0, 8);
        List<Product> products;
        if (q != null && !q.trim().isEmpty()) {
            products = adminProductService
                    .findByNameOrMasp(q.trim(), topEight)
                    .getContent();
        } else {
            products = adminProductService.getAllProducts(topEight).getContent();
        }
        model.addAttribute("homeProducts", products);
        model.addAttribute("homeCategories", categoryService.findAll());
        model.addAttribute("q", q);
        return "user/home"; // Trả về file home.html trong /templates
    }
}
