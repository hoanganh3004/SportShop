package com.library.sportshop.controller.admin;

import com.library.sportshop.entity.Product;
import com.library.sportshop.entity.Category;
import com.library.sportshop.service.AdminProductService;
import com.library.sportshop.service.AdminCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/adproduct")
public class AdminProductController {

    @Autowired
    private AdminProductService adminProductService;

    @Autowired
    private AdminCategoryService adminCategoryService;

    @GetMapping
    public String listProducts(@RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "10") int size,
                               @RequestParam(required = false) String keyword,
                               @RequestParam(required = false) Integer categoryId,
                               Model model) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Product> products;

            //  search
            if ((keyword != null && !keyword.trim().isEmpty()) || (categoryId != null && categoryId > 0)) {
                products = adminProductService.searchProducts(keyword, categoryId, pageable);
            } else {
                products = adminProductService.getAllProducts(pageable);
            }

            model.addAttribute("products", products);
            model.addAttribute("currentPage", page + 1);
            model.addAttribute("totalPages", products.getTotalPages());
            model.addAttribute("keyword", keyword);
            model.addAttribute("categoryId", categoryId);
            model.addAttribute("categories", adminCategoryService.getAllCategories());

            return "admin/adminProduct";
        } catch (Exception e) {
            model.addAttribute("error", "Lỗi khi tải danh sách sản phẩm: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/new")
    public String newProductForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", adminCategoryService.getAllCategories());
        return "admin/addProduct";
    }

    @PostMapping("/save")
    public String saveProduct(@RequestParam(value = "name", required = false) String name,
                              @RequestParam(value = "masp", required = false) String masp,
                              @RequestParam(value = "price", required = false) String priceStr,
                              @RequestParam(value = "quantity", required = false) String quantityStr,
                              @RequestParam(value = "description", required = false) String description,
                              @RequestParam(value = "size", required = false) String size,
                              @RequestParam(value = "color", required = false) String color,
                              @RequestParam(value = "categoryId", required = false) Integer categoryId,
                              @RequestParam(value = "newImages", required = false) MultipartFile[] newImages,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        try {
            System.out.println("=== SAVE PRODUCT DEBUG ===");
            System.out.println("name: " + name);
            System.out.println("masp: " + masp);
            System.out.println("price: " + priceStr);
            System.out.println("quantity: " + quantityStr);
            System.out.println("categoryId: " + categoryId);
            System.out.println("images: " + (newImages != null ? newImages.length : "null"));

            // Validate dữ liệu đầu vào
            if (name == null || name.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Tên sản phẩm không được để trống!");
                return "redirect:/adproduct/new";
            }

            if (masp == null || masp.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Mã sản phẩm không được để trống!");
                return "redirect:/adproduct/new";
            }

            // Parse price
            Double price;
            try {
                price = Double.parseDouble(priceStr);
                if (price <= 0) {
                    redirectAttributes.addFlashAttribute("error", "Giá sản phẩm phải lớn hơn 0!");
                    return "redirect:/adproduct/new";
                }
            } catch (NumberFormatException e) {
                redirectAttributes.addFlashAttribute("error", "Giá sản phẩm không hợp lệ!");
                return "redirect:/adproduct/new";
            }

            // Parse quantity
            Integer quantity;
            try {
                quantity = Integer.parseInt(quantityStr);
                if (quantity < 0) {
                    redirectAttributes.addFlashAttribute("error", "Số lượng sản phẩm không được âm!");
                    return "redirect:/adproduct/new";
                }
            } catch (NumberFormatException e) {
                redirectAttributes.addFlashAttribute("error", "Số lượng sản phẩm không hợp lệ!");
                return "redirect:/adproduct/new";
            }

            // Kiểm tra có ảnh không
            boolean hasImages = newImages != null && newImages.length > 0 && !newImages[0].isEmpty();
            if (!hasImages) {
                redirectAttributes.addFlashAttribute("error", "Vui lòng chọn ít nhất một ảnh cho sản phẩm!");
                return "redirect:/adproduct/new";
            }

            // Tạo Product object
            Product product = new Product();
            product.setName(name.trim());
            product.setMasp(masp.trim());
            product.setPrice(java.math.BigDecimal.valueOf(price));
            product.setQuantity(quantity);
            product.setDescription(description);
            product.setSize(size);
            product.setColor(color);

            // Set category nếu có
            if (categoryId != null && categoryId > 0) {
                Category category = new Category();
                category.setId(categoryId);
                product.setCategory(category);
            }

            // Lưu sản phẩm với ảnh
            adminProductService.saveProductWithImages(product, newImages);
            redirectAttributes.addFlashAttribute("success", " Thêm sản phẩm thành công!");

        } catch (Exception e) {
            System.err.println("Error saving product: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", " Thêm thất bại: " + e.getMessage());
            return "redirect:/adproduct/new";
        }
        return "redirect:/adproduct";
    }

    @GetMapping("/detail/{id}")
    public String viewDetail(@PathVariable Integer id, Model model) {
        try {
            System.out.println("Fetching product with id: " + id);
            Product product = adminProductService.getProductByIdWithImages(id);

            if (product == null) {
                model.addAttribute("error", "Sản phẩm không tồn tại với ID: " + id);
                return "admin/productDetail";
            }

            model.addAttribute("product", product);
            model.addAttribute("categories", adminCategoryService.getAllCategories());
            return "admin/productDetail";
        } catch (Exception e) {
            System.out.println("Error in viewDetail: " + e.getMessage());
            model.addAttribute("error", "Lỗi khi tải chi tiết sản phẩm: " + e.getMessage());
            return "admin/productDetail";
        }
    }

    @PostMapping("/detail/{id}")
    public String updateDetail(@PathVariable Integer id,
                               @ModelAttribute("product") Product product,
                               @RequestParam(value = "categoryId", required = false) Integer categoryId,
                               @RequestParam(value = "newImages", required = false) MultipartFile[] newImages,
                               RedirectAttributes redirectAttributes) {
        try {
            // Validate dữ liệu đầu vào
            if (product.getName() == null || product.getName().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Tên sản phẩm không được để trống!");
                return "redirect:/adproduct/detail/" + id;
            }

            if (product.getMasp() == null || product.getMasp().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Mã sản phẩm không được để trống!");
                return "redirect:/adproduct/detail/" + id;
            }

            if (product.getPrice() == null || product.getPrice().doubleValue() <= 0) {
                redirectAttributes.addFlashAttribute("error", "Giá sản phẩm phải lớn hơn 0!");
                return "redirect:/adproduct/detail/" + id;
            }

            if (product.getQuantity() == null || product.getQuantity() < 0) {
                redirectAttributes.addFlashAttribute("error", "Số lượng sản phẩm không được âm!");
                return "redirect:/adproduct/detail/" + id;
            }

            // Set ID cho product
            product.setId(id);

            // Set category nếu có
            if (categoryId != null) {
                Category category = new Category();
                category.setId(categoryId);
                product.setCategory(category);
            }

            // Kiểm tra có ảnh mới không
            boolean hasNewImages = newImages != null && newImages.length > 0 && !newImages[0].isEmpty();

            if (hasNewImages) {
                // Cập nhật sản phẩm với ảnh mới
                adminProductService.updateProductWithImages(product, newImages);
                redirectAttributes.addFlashAttribute("success", " Cập nhật sản phẩm và ảnh thành công!");
            } else {
                // Chỉ cập nhật thông tin sản phẩm, giữ nguyên ảnh cũ
                adminProductService.saveProduct(product);
                redirectAttributes.addFlashAttribute("success", " Cập nhật thông tin sản phẩm thành công!");
            }

        } catch (Exception e) {
            System.err.println("Error updating product: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", " Cập nhật thất bại: " + e.getMessage());
            return "redirect:/adproduct/detail/" + id;
        }
        return "redirect:/adproduct";
    }

    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Integer id,
                                RedirectAttributes redirectAttributes) {
        try {
            adminProductService.deleteProduct(id);
            redirectAttributes.addFlashAttribute("success", "Xóa sản phẩm thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Xóa thất bại: " + e.getMessage());
        }
        return "redirect:/adproduct";
    }
}