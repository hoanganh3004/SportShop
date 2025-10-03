package com.library.sportshop.controller.admin;

import com.library.sportshop.entity.Category;
import com.library.sportshop.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/adcategory")
public class AdminCategoryController {

    @Autowired
    private CategoryService categoryService;

    private static final int PAGE_SIZE = 5;

    // Hiển thị danh sách + tìm kiếm + phân trang
    @GetMapping
    public String listCategories(@RequestParam(value = "page", defaultValue = "1") int page,
                                 @RequestParam(value = "keyword", required = false) String keyword,
                                 Model model) {
        Page<Category> categories = categoryService.getAllCategories(keyword, PageRequest.of(page - 1, PAGE_SIZE));

        model.addAttribute("categories", categories);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", categories.getTotalPages());
        model.addAttribute("keyword", keyword);

        return "admin/adminCategory";
    }

    // Form thêm mới
    @GetMapping("/new")
    public String newCategoryForm(Model model) {
        model.addAttribute("category", new Category());
        return "admin/addCategory";
    }

    // Lưu (thêm/sửa)
    @PostMapping("/save")
    public String saveCategory(@ModelAttribute("category") Category category, Model model) {
        try {
            categoryService.saveCategory(category);
            model.addAttribute("success", "Lưu danh mục thành công!");
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi xảy ra khi lưu danh mục!");
        }
        return "redirect:/adcategory";
    }

    // Form sửa
    @GetMapping("/edit/{id}")
    public String editCategoryForm(@PathVariable("id") Integer id, Model model) {
        Optional<Category> category = categoryService.getCategoryById(id);
        if (category.isPresent()) {
            model.addAttribute("category", category.get());
            return "admin/editCategory";
        } else {
            model.addAttribute("error", "Không tìm thấy danh mục!");
            return "redirect:/adcategory";
        }
    }

    // Xóa
    @GetMapping("/delete/{id}")
    public String deleteCategory(@PathVariable("id") Integer id, Model model) {
        try {
            categoryService.deleteCategory(id);
            model.addAttribute("success", "Xóa danh mục thành công!");
        } catch (Exception e) {
            model.addAttribute("error", "Không thể xóa danh mục!");
        }
        return "redirect:/adcategory";
    }
}
