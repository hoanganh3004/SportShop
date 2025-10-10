package com.library.sportshop.controller.user;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;

import com.library.sportshop.entity.Account;
import com.library.sportshop.dto.CartItemResponse;
import com.library.sportshop.service.AccountService;
import com.library.sportshop.service.CartService;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;

import java.security.Principal;
import java.util.List;

@Controller
public class CheckoutController {
    @Autowired
    private AccountService accountService;

    @Autowired
    private CartService cartService;

    @GetMapping("/checkout")
    public String checkout(Model model, Principal principal, HttpSession session) {
        // Yêu cầu đăng nhập
        if (principal == null) {
            return "redirect:/login";
        }

        Account account = accountService.findByUsername(principal.getName());
        if (account == null) {
            return "redirect:/login";
        }

        // Nạp cart từ DB
        String userCode = account.getCode();
        var items = cartService.findByUserCode(userCode);
        List<CartItemResponse> cartItems = cartService.mapToDto(items);

        // Tính tổng số lượng và tiền
        int totalQty = cartService.countQuantityByUserCode(userCode);
        session.setAttribute("cartQty", totalQty);

        long subtotal = 0L;
        for (CartItemResponse it : cartItems) {
            if (it.getProduct() != null) {
                long price = it.getProduct().getPrice() == null ? 0L : it.getProduct().getPrice().longValue();
                int qty = it.getQuantity() == null ? 0 : it.getQuantity();
                subtotal += price * qty;
            }
        }

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("total", subtotal); // chưa có phí ship/giảm giá -> total = subtotal
        model.addAttribute("account", account);

        return "user/checkout";
    }
}
