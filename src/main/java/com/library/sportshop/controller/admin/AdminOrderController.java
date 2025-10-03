package com.library.sportshop.controller.admin;

import com.library.sportshop.entity.Order;
import com.library.sportshop.service.OrderService;
import com.library.sportshop.service.AdminProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/adorder")
public class AdminOrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private AdminProductService adminProductService; //

    // 📦 Danh sách đơn hàng
    @GetMapping
    public String listOrders(Model model,
                             @RequestParam(defaultValue = "1") int page,
                             @RequestParam(required = false) String keyword) {
        Page<Order> orders = orderService.getAllOrders(keyword, PageRequest.of(page - 1, 5));

        model.addAttribute("orders", orders);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orders.getTotalPages());
        model.addAttribute("keyword", keyword);

        return "admin/adminOrder";
    }

    //  Xem chi tiết
    @GetMapping("/detail/{id}")
    public String detailOrder(@PathVariable Integer id, Model model) {
        Optional<Order> order = orderService.getOrderById(id);
        if (order.isEmpty()) {
            model.addAttribute("error", "Không tìm thấy đơn hàng!");
            return "admin/adminOrder";
        }
        model.addAttribute("order", order.get());
        return "admin/orderDetail";
    }

    // Tạo đơn hàng mới
    @GetMapping("/new")
    public String addOrderForm(Model model) {
        model.addAttribute("order", new Order());
        model.addAttribute("products", adminProductService.getAllProductsForDropdown().getContent());
        return "admin/addOrder";
    }

    @PostMapping("/save")
    public String saveOrder(@ModelAttribute Order order,
                            @RequestParam Integer productId,
                            @RequestParam Integer quantity,
                            Model model) {
        orderService.createOrderWithItem(order, productId, quantity);
        model.addAttribute("success", "Tạo đơn hàng thành công!");
        return "redirect:/adorder";
    }

    //  Cập nhật trạng thái
    @GetMapping("/update/{id}")
    public String updateOrderForm(@PathVariable Integer id, Model model) {
        Optional<Order> order = orderService.getOrderById(id);
        if (order.isEmpty()) {
            model.addAttribute("error", "Không tìm thấy đơn hàng!");
            return "redirect:/adorder";
        }
        model.addAttribute("order", order.get());
        return "admin/editOrder";
    }

    @PostMapping("/update")
    public String updateOrder(@RequestParam Integer id,
                              @RequestParam String status,
                              @RequestParam(required = false) String cancelReason,
                              Model model) {
        orderService.updateOrderStatus(id, status, cancelReason);
        model.addAttribute("success", "Cập nhật trạng thái thành công!");
        return "redirect:/adorder";
    }

    //  Xóa đơn hàng
    @GetMapping("/delete/{id}")
    public String deleteOrder(@PathVariable Integer id, Model model) {
        orderService.deleteOrder(id);
        model.addAttribute("success", "Xóa đơn hàng thành công!");
        return "redirect:/adorder";
    }
}
