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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ProductController {

    @Autowired
    private AdminProductService adminProductService;

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/product") // khi truy cập localhost:8080/
    public String product(Model model,
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "categoryId", required = false) Integer categoryId,
            @RequestParam(value = "minPrice", required = false) Long minPrice,
            @RequestParam(value = "maxPrice", required = false) Long maxPrice) {
        Pageable firstPage = PageRequest.of(0, 20);

        // Sử dụng service thay vì repository trực tiếp
        List<Product> products = adminProductService.findByFilters(
                q != null && !q.trim().isEmpty() ? q.trim() : null,
                minPrice,
                maxPrice,
                categoryId != null && categoryId > 0 ? categoryId : null,
                firstPage).getContent();

        model.addAttribute("products", products);
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("q", q);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        return "user/product"; // Trả về file product.html trong /templates
    }

    @GetMapping("/product-detail/{id}")
    public String productdetail(@PathVariable Integer id, Model model) {
        Product product = adminProductService.getProductByIdWithImages(id);
        model.addAttribute("product", product);
        // Related products (same category), exclude current
        List<Product> relatedProducts;
        if (product != null && product.getCategory() != null) {
            Pageable firstEight = PageRequest.of(0, 8);
            relatedProducts = adminProductService
                    .findByCategoryId(product.getCategory().getId(), firstEight)
                    .getContent()
                    .stream()
                    .filter(p -> p.getId() != null && !p.getId().equals(id))
                    .collect(Collectors.toList());
        } else {
            Pageable firstEight = PageRequest.of(0, 8);
            relatedProducts = adminProductService.getAllProducts(firstEight)
                    .getContent()
                    .stream()
                    .filter(p -> p.getId() != null && !p.getId().equals(id))
                    .collect(Collectors.toList());
        }
        model.addAttribute("relatedProducts", relatedProducts);
        model.addAttribute("categories", categoryService.findAll());
        return "user/product-detail";
    }
}
