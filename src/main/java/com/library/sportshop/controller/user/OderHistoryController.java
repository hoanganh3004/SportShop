package com.library.sportshop.controller.user;

import com.library.sportshop.entity.Account;
import com.library.sportshop.entity.Order;
import com.library.sportshop.service.AccountService;
import com.library.sportshop.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Optional;

@Controller
public class OderHistoryController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private AccountService accountService;

    @GetMapping("/oderhistory")
    // trang lịch sử đơn hàng của người dùng
    public String oderhistory(@RequestParam(defaultValue = "1") int page,
                              Model model,
                              Principal principal) {
        // yêu cầu đăng nhập
        if (principal == null) return "redirect:/login";
        Account acc = accountService.findByUsername(principal.getName());
        if (acc == null) return "redirect:/login";

        // lấy danh sách đơn của chính người dùng
        Page<Order> orders = orderService.getOrdersByUser(acc.getCode(), PageRequest.of(page - 1, 10));
        model.addAttribute("orders", orders);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orders.getTotalPages());
        return "user/oderHistory";
    }

    // xem chi tiết đơn của chính mình (tái sử dụng view admin/orderDetail)
    @GetMapping("/oderhistory/detail/{id}")
    public String orderDetail(@PathVariable Integer id,
                              Principal principal,
                              Model model) {
        // yêu cầu đăng nhập
        if (principal == null) return "redirect:/login";
        Account acc = accountService.findByUsername(principal.getName());
        if (acc == null) return "redirect:/login";
        Optional<Order> opt = orderService.getOrderById(id);
        // chặn truy cập đơn hàng của người khác
        if (opt.isEmpty() || !acc.getCode().equals(opt.get().getUserCode())) {
            model.addAttribute("error", "Không tìm thấy đơn hàng của bạn");
            return "user/oderHistory";
        }
        model.addAttribute("order", opt.get());
        return "admin/orderDetail"; // tái sử dụng view chi tiết có sẵn
    }

    // hủy đơn khi đang Chờ xác nhận
    @PostMapping("/oderhistory/cancel/{id}")
    public String cancelOrder(@PathVariable Integer id,
                              @RequestParam(required = false) String reason,
                              Principal principal,
                              RedirectAttributes redirectAttributes) {
        // yêu cầu đăng nhập
        if (principal == null) return "redirect:/login";
        Account acc = accountService.findByUsername(principal.getName());
        if (acc == null) return "redirect:/login";

        Optional<Order> opt = orderService.getOrderById(id);
        // chỉ cho phép hủy đơn của chính mình
        if (opt.isEmpty() || !acc.getCode().equals(opt.get().getUserCode())) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn hàng của bạn");
            return "redirect:/oderhistory";
        }

        Order order = opt.get();
        // chỉ cho hủy khi đang Chờ xác nhận
        if (!"Chờ xác nhận".equals(order.getStatus())) {
            redirectAttributes.addFlashAttribute("error", "Chỉ có thể hủy đơn khi đang Chờ xác nhận");
            return "redirect:/oderhistory";
        }

        orderService.updateOrderStatus(id, "Đã hủy", reason);
        redirectAttributes.addFlashAttribute("success", "Hủy đơn hàng thành công");
        return "redirect:/oderhistory";
    }
}
