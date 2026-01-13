package com.library.sportshop.controller.admin;

import com.library.sportshop.entity.Product;
import com.library.sportshop.entity.Category;
import com.library.sportshop.service.AdminProductService;
import com.library.sportshop.service.AdminCategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;

@Controller
@RequestMapping("/adproduct")
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductController {

    private static final Logger log = LoggerFactory.getLogger(AdminProductController.class);

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

            // search
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
            log.debug("Saving product - name: {}, masp: {}, price: {}, quantity: {}, categoryId: {}, images: {}",
                    name, masp, priceStr, quantityStr, categoryId, (newImages != null ? newImages.length : 0));

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
            log.error("Error saving product", e);
            redirectAttributes.addFlashAttribute("error", " Thêm thất bại: " + e.getMessage());
            return "redirect:/adproduct/new";
        }
        return "redirect:/adproduct";
    }

    @GetMapping("/detail/{id}")
    public String viewDetail(@PathVariable Integer id, Model model) {
        try {
            log.debug("Fetching product with id: {}", id);
            Product product = adminProductService.getProductByIdWithImages(id);

            if (product == null) {
                model.addAttribute("error", "Sản phẩm không tồn tại với ID: " + id);
                return "admin/productDetail";
            }

            model.addAttribute("product", product);
            model.addAttribute("categories", adminCategoryService.getAllCategories());
            return "admin/productDetail";
        } catch (Exception e) {
            log.error("Error in viewDetail for product id: {}", id, e);
            model.addAttribute("error", "Lỗi khi tải chi tiết sản phẩm: " + e.getMessage());
            return "admin/productDetail";
        }
    }

    @GetMapping("/edit/{id}")
    public String editProductForm(@PathVariable Integer id, Model model) {
        try {
            Product product = adminProductService.getProductByIdWithImages(id);
            if (product == null) {
                model.addAttribute("error", "Sản phẩm không tồn tại!");
                return "redirect:/adproduct";
            }
            model.addAttribute("product", product);
            model.addAttribute("categories", adminCategoryService.getAllCategories());
            return "admin/productEdit";
        } catch (Exception e) {
            model.addAttribute("error", "Lỗi tải form sửa: " + e.getMessage());
            return "redirect:/adproduct";
        }
    }

    @PostMapping("/edit/{id}")
    public String updateProduct(@PathVariable Integer id,
            @ModelAttribute("product") Product product,
            @RequestParam(value = "categoryId", required = false) Integer categoryId,
            @RequestParam(value = "newImages", required = false) MultipartFile[] newImages,
            RedirectAttributes redirectAttributes,
            Model model) { // Thêm Model để trả về view edit khi lỗi
        try {
            // Validate dữ liệu đầu vào
            if (product.getName() == null || product.getName().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Tên sản phẩm không được để trống!");
                return "redirect:/adproduct/edit/" + id;
            }

            if (product.getMasp() == null || product.getMasp().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Mã sản phẩm không được để trống!");
                return "redirect:/adproduct/edit/" + id;
            }

            if (product.getPrice() == null || product.getPrice().doubleValue() <= 0) {
                redirectAttributes.addFlashAttribute("error", "Giá sản phẩm phải lớn hơn 0!");
                return "redirect:/adproduct/edit/" + id;
            }

            if (product.getQuantity() == null || product.getQuantity() < 0) {
                redirectAttributes.addFlashAttribute("error", "Số lượng sản phẩm không được âm!");
                return "redirect:/adproduct/edit/" + id;
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
                // Cập nhật sản phẩm với ảnh mới (Service đã sửa để APPEND ảnh)
                adminProductService.updateProductWithImages(product, newImages);
                redirectAttributes.addFlashAttribute("success", " Cập nhật sản phẩm và thêm ảnh mới thành công!");
            } else {
                // Chỉ cập nhật thông tin sản phẩm, giữ nguyên ảnh cũ
                adminProductService.saveProduct(product);
                redirectAttributes.addFlashAttribute("success", " Cập nhật thông tin sản phẩm thành công!");
            }

        } catch (Exception e) {
            log.error("Error updating product with id: {}", id, e);
            redirectAttributes.addFlashAttribute("error", " Cập nhật thất bại: " + e.getMessage());
            return "redirect:/adproduct/edit/" + id;
        }
        return "redirect:/adproduct";
    }

    @DeleteMapping("/image/{imageId}")
    @ResponseBody
    public ResponseEntity<?> deleteImage(@PathVariable Integer imageId) {
        try {
            adminProductService.deleteImage(imageId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi xóa ảnh: " + e.getMessage());
        }
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