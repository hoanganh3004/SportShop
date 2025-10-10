package com.library.sportshop.controller.user;

import com.library.sportshop.entity.Product;
import com.library.sportshop.repository.ProductRepository;
import com.library.sportshop.service.CategoryService;
import com.library.sportshop.service.AdminProductService;
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
    private ProductRepository productRepository;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AdminProductService adminProductService;

    @GetMapping("/product")   // khi truy cập localhost:8080/
    public String product(Model model,
                          @RequestParam(value = "q", required = false) String q,
                          @RequestParam(value = "categoryId", required = false) Integer categoryId) {
        Pageable firstPage = PageRequest.of(0, 20);
        List<Product> products;
        if (categoryId != null && categoryId > 0) {
            products = productRepository.findByCategoryId(categoryId, firstPage).getContent();
        } else if (q != null && !q.trim().isEmpty()) {
            String s = q.trim();
            products = productRepository.findByNameContainingOrMaspContaining(s, s, firstPage).getContent();
        } else {
            products = productRepository.findAll(firstPage).getContent();
        }
        model.addAttribute("products", products);
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("q", q);
        model.addAttribute("categoryId", categoryId);
        return "user/product";  // Trả về file product.html trong /templates
    }

    @GetMapping("/product-detail/{id}")
    public String productdetail(@PathVariable Integer id, Model model) {
        Product product = adminProductService.getProductByIdWithImages(id);
        model.addAttribute("product", product);
        // Related products (same category), exclude current
        List<Product> relatedProducts;
        if (product != null && product.getCategory() != null) {
            Pageable firstEight = PageRequest.of(0, 8);
            relatedProducts = productRepository
                    .findByCategoryId(product.getCategory().getId(), firstEight)
                    .getContent()
                    .stream()
                    .filter(p -> p.getId() != null && !p.getId().equals(id))
                    .collect(Collectors.toList());
        } else {
            Pageable firstEight = PageRequest.of(0, 8);
            relatedProducts = productRepository.findAll(firstEight)
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
