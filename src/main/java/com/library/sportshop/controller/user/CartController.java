package com.library.sportshop.controller.user;

import com.library.sportshop.entity.CartItem;
import com.library.sportshop.entity.Product;
import com.library.sportshop.repository.CartItemRepository;
import com.library.sportshop.repository.ProductRepository;
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
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

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
        if (principal == null) return ResponseEntity.status(401).body("UNAUTHORIZED");

        Account account = accountService.findByUsername(principal.getName());
        if (account == null) return ResponseEntity.status(401).body("UNAUTHORIZED");

        String userCode = account.getCode();

        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) return ResponseEntity.badRequest().body("INVALID_PRODUCT");

        CartItem item = cartItemRepository.findByUserCodeAndProduct_Id(userCode, productId)
                .orElseGet(() -> {
                    CartItem ci = new CartItem();
                    ci.setUserCode(userCode);
                    ci.setProduct(product);
                    ci.setQuantity(0);
                    return ci;
                });

        item.setQuantity(item.getQuantity() + quantity);
        cartItemRepository.save(item);

        Integer totalQty = cartItemRepository.countQuantityByUserCode(userCode);
        session.setAttribute("cartQty", totalQty);
        return ResponseEntity.ok(totalQty);
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
        if (principal == null) return ResponseEntity.status(401).body("UNAUTHORIZED");
        Account account = accountService.findByUsername(principal.getName());
        if (account == null) return ResponseEntity.status(401).body("UNAUTHORIZED");
        String userCode = account.getCode();

        CartItem item = cartItemRepository.findByUserCodeAndProduct_Id(userCode, productId).orElse(null);
        if (item == null) return ResponseEntity.badRequest().body("ITEM_NOT_FOUND");

        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) return ResponseEntity.badRequest().body("INVALID_PRODUCT");
        Integer stock = product.getQuantity() == null ? Integer.MAX_VALUE : product.getQuantity();
        if (item.getQuantity() >= stock) {
            // Không cho vượt quá tồn kho
            int totalQtyNoChange = cartItemRepository.countQuantityByUserCode(userCode);
            session.setAttribute("cartQty", totalQtyNoChange);
            return ResponseEntity.status(409).body("LIMIT_REACHED");
        }

        item.setQuantity(item.getQuantity() + 1);
        cartItemRepository.save(item);

        int totalQty = cartItemRepository.countQuantityByUserCode(userCode);
        session.setAttribute("cartQty", totalQty);
        return ResponseEntity.ok(totalQty);
    }

    // giảm số lượng 1 đơn vị (không dưới 1)
    @PostMapping("/decrease")
    @ResponseBody
    public ResponseEntity<?> decreaseQuantity(@RequestParam Integer productId,
                                              Principal principal,
                                              HttpSession session) {
        if (principal == null) return ResponseEntity.status(401).body("UNAUTHORIZED");
        Account account = accountService.findByUsername(principal.getName());
        if (account == null) return ResponseEntity.status(401).body("UNAUTHORIZED");
        String userCode = account.getCode();

        CartItem item = cartItemRepository.findByUserCodeAndProduct_Id(userCode, productId).orElse(null);
        if (item == null) return ResponseEntity.badRequest().body("ITEM_NOT_FOUND");

        if (item.getQuantity() > 1) {
            item.setQuantity(item.getQuantity() - 1);
            cartItemRepository.save(item);
        } else {
            // không giảm dưới 1, giữ nguyên
        }

        int totalQty = cartItemRepository.countQuantityByUserCode(userCode);
        session.setAttribute("cartQty", totalQty);
        return ResponseEntity.ok(totalQty);
    }

    // xoá sản phẩm khỏi giỏ
    @PostMapping("/remove")
    @ResponseBody
    public ResponseEntity<?> removeItem(@RequestParam Integer productId,
                                        Principal principal,
                                        HttpSession session) {
        if (principal == null) return ResponseEntity.status(401).body("UNAUTHORIZED");
        Account account = accountService.findByUsername(principal.getName());
        if (account == null) return ResponseEntity.status(401).body("UNAUTHORIZED");
        String userCode = account.getCode();

        cartItemRepository.deleteByUserCodeAndProduct_Id(userCode, productId);

        int totalQty = cartItemRepository.countQuantityByUserCode(userCode);
        session.setAttribute("cartQty", totalQty);
        return ResponseEntity.ok(totalQty);
    }
}
