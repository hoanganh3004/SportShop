package com.library.sportshop.controller.user;

import com.library.sportshop.entity.CartItem;
import com.library.sportshop.service.AccountService;
import com.library.sportshop.entity.Account;
import com.library.sportshop.service.CartService;
import com.library.sportshop.dto.CartItemResponseDTO;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.ArrayList;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private CartService cartService;

    // thêm sản phẩm vào giỏ
    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<?> addToCart(@RequestParam Integer productId,
            @RequestParam(defaultValue = "1") Integer quantity,
            Principal principal,
            HttpSession session) {
        if (principal == null)
            return ResponseEntity.status(401).body("UNAUTHORIZED");

        Account account = accountService.findByUsername(principal.getName());
        if (account == null)
            return ResponseEntity.status(401).body("UNAUTHORIZED");

        try {
            Integer totalQty = cartService.addToCart(productId, quantity, account.getCode());
            session.setAttribute("cartQty", totalQty);
            return ResponseEntity.ok(totalQty);
        } catch (RuntimeException e) {
            String msg = e.getMessage();
            if ("INVALID_PRODUCT".equals(msg))
                return ResponseEntity.badRequest().body("INVALID_PRODUCT");
            if ("OUT_OF_STOCK".equals(msg))
                return ResponseEntity.status(409).body("OUT_OF_STOCK");
            if ("INSUFFICIENT_STOCK".equals(msg))
                return ResponseEntity.status(409).body("INSUFFICIENT_STOCK");
            return ResponseEntity.status(500).body("INTERNAL_SERVER_ERROR");
        }
    }

    // lấy danh sách item trong giỏ
    @GetMapping("/items")
    @ResponseBody
    public List<CartItemResponseDTO> getCartItems(Principal principal, HttpSession session) {
        if (principal == null) {
            return new ArrayList<>();
        }

        Account account = accountService.findByUsername(principal.getName());
        if (account == null) {
            return new ArrayList<>();
        }

        String userCode = account.getCode();

        List<CartItem> items = cartService.findByUserCode(userCode);

        int totalQty = cartService.countQuantityByUserCode(userCode);
        session.setAttribute("cartQty", totalQty);

        return cartService.mapToDto(items);
    }

    // tăng số lượng 1 đơn vị cho sản phẩm trong giỏ
    @PostMapping("/increase")
    @ResponseBody
    public ResponseEntity<?> increaseQuantity(@RequestParam Integer productId,
            Principal principal,
            HttpSession session) {
        if (principal == null)
            return ResponseEntity.status(401).body("UNAUTHORIZED");
        Account account = accountService.findByUsername(principal.getName());
        if (account == null)
            return ResponseEntity.status(401).body("UNAUTHORIZED");

        try {
            Integer totalQty = cartService.increaseQuantity(productId, account.getCode());
            session.setAttribute("cartQty", totalQty);
            return ResponseEntity.ok(totalQty);
        } catch (RuntimeException e) {
            String msg = e.getMessage();
            if ("ITEM_NOT_FOUND".equals(msg))
                return ResponseEntity.badRequest().body("ITEM_NOT_FOUND");
            if ("INVALID_PRODUCT".equals(msg))
                return ResponseEntity.badRequest().body("INVALID_PRODUCT");
            if ("LIMIT_REACHED".equals(msg))
                return ResponseEntity.status(409).body("LIMIT_REACHED");
            return ResponseEntity.status(500).body("INTERNAL_SERVER_ERROR");
        }
    }

    // giảm số lượng 1 đơn vị (không dưới 1)
    @PostMapping("/decrease")
    @ResponseBody
    public ResponseEntity<?> decreaseQuantity(@RequestParam Integer productId,
            Principal principal,
            HttpSession session) {
        if (principal == null)
            return ResponseEntity.status(401).body("UNAUTHORIZED");
        Account account = accountService.findByUsername(principal.getName());
        if (account == null)
            return ResponseEntity.status(401).body("UNAUTHORIZED");

        try {
            Integer totalQty = cartService.decreaseQuantity(productId, account.getCode());
            session.setAttribute("cartQty", totalQty);
            return ResponseEntity.ok(totalQty);
        } catch (RuntimeException e) {
            if ("ITEM_NOT_FOUND".equals(e.getMessage()))
                return ResponseEntity.badRequest().body("ITEM_NOT_FOUND");
            return ResponseEntity.status(500).body("INTERNAL_SERVER_ERROR");
        }
    }

    // xoá sản phẩm khỏi giỏ
    @PostMapping("/remove")
    @ResponseBody
    public ResponseEntity<?> removeItem(@RequestParam Integer productId,
            Principal principal,
            HttpSession session) {
        if (principal == null)
            return ResponseEntity.status(401).body("UNAUTHORIZED");
        Account account = accountService.findByUsername(principal.getName());
        if (account == null)
            return ResponseEntity.status(401).body("UNAUTHORIZED");

        cartService.removeItem(productId, account.getCode());
        Integer totalQty = cartService.countQuantityByUserCode(account.getCode());
        session.setAttribute("cartQty", totalQty);
        return ResponseEntity.ok(totalQty);
    }
}
