package com.library.sportshop.service.impl;

import com.library.sportshop.entity.Order;
import com.library.sportshop.entity.OrderItem;
import com.library.sportshop.entity.Product;
import com.library.sportshop.entity.CartItem;
import com.library.sportshop.repository.OrderItemRepository;
import com.library.sportshop.repository.OrderRepository;
import com.library.sportshop.repository.ProductRepository;
import com.library.sportshop.repository.CartItemRepository;
import com.library.sportshop.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Override
    public Page<Order> getAllOrders(String keyword, Pageable pageable) {
        if (keyword != null && !keyword.isEmpty()) {
            return orderRepository.findByRecipientNameContainingIgnoreCaseOrRecipientEmailContainingIgnoreCase(
                    keyword, keyword, pageable);
        }
        return orderRepository.findAll(pageable);
    }

    @Override
    public Optional<Order> getOrderById(Integer id) {
        return orderRepository.findById(id);
    }

    @Override
    public Order saveOrder(Order order) {
        order.setOrderDate(LocalDateTime.now());
        if (order.getStatus() == null) {
            order.setStatus("Chờ xác nhận");
        }
        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public Order createOrderWithItem(Order order, Integer productId, Integer quantity) {
        order.setOrderDate(LocalDateTime.now());
        if (order.getStatus() == null) {
            order.setStatus("Chờ xác nhận");
        }
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không tồn tại"));

        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Số lượng không hợp lệ");
        }

        // Tính đơn giá và tổng tiền
        BigDecimal unitPrice = product.getPrice();
        BigDecimal total = unitPrice.multiply(BigDecimal.valueOf(quantity));
        order.setTotalAmount(total);

        Order saved = orderRepository.save(order);

        OrderItem item = new OrderItem();
        item.setOrder(saved);
        item.setProduct(product);
        item.setQuantity(quantity);
        item.setUnitPrice(unitPrice);
        item.setProductName(product.getName());
        item.setProductMasp(product.getMasp());
        item.setProductDescription(product.getDescription());
        item.setProductSize(product.getSize());
        item.setProductColor(product.getColor());
        // lấy ảnh đầu tiên nếu có
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            item.setProductImage(product.getImages().get(0).getImageUrl());
        }
        orderItemRepository.save(item);

        return saved;
    }

    @Override
    @Transactional
    public Order createOrderFromCart(String userCode,
                                     String recipientName,
                                     String recipientPhone,
                                     String recipientAddress,
                                     String recipientEmail) {
        if (userCode == null || userCode.isBlank()) {
            throw new IllegalArgumentException("userCode không hợp lệ");
        }
        List<CartItem> cartItems = cartItemRepository.findByUserCode(userCode);
        if (cartItems == null || cartItems.isEmpty()) {
            throw new IllegalStateException("Giỏ hàng trống");
        }

        // Trừ kho an toàn (atomic) cho từng sản phẩm trong giỏ: nếu không đủ hàng -> ném lỗi và rollback
        for (CartItem ci : cartItems) {
            Product p = ci.getProduct();
            if (p == null || p.getId() == null) continue;
            int qty = ci.getQuantity() == null ? 0 : ci.getQuantity();
            if (qty <= 0) continue;
            int updated = productRepository.decrementIfEnough(p.getId(), qty);
            if (updated == 0) {
                String name = p.getName() == null ? "Sản phẩm" : p.getName();
                throw new IllegalStateException("Sản phẩm '" + name + "' không đủ hàng để đặt.");
            }
        }

        Order order = new Order();
        order.setUserCode(userCode);
        order.setRecipientName(recipientName);
        order.setRecipientPhone(recipientPhone);
        order.setRecipientAddress(recipientAddress);
        order.setRecipientEmail(recipientEmail);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("Chờ xác nhận");

        BigDecimal total = BigDecimal.ZERO;
        for (CartItem ci : cartItems) {
            Product p = ci.getProduct();
            BigDecimal price = (p != null && p.getPrice() != null) ? p.getPrice() : BigDecimal.ZERO;
            int qty = ci.getQuantity() == null ? 0 : ci.getQuantity();
            total = total.add(price.multiply(BigDecimal.valueOf(qty)));
        }
        order.setTotalAmount(total);

        Order saved = orderRepository.save(order);

        for (CartItem ci : cartItems) {
            Product product = ci.getProduct();
            if (product == null) continue;
            OrderItem item = new OrderItem();
            item.setOrder(saved);
            item.setProduct(product);
            item.setQuantity(ci.getQuantity());
            item.setUnitPrice(product.getPrice());
            item.setProductName(product.getName());
            item.setProductMasp(product.getMasp());
            item.setProductDescription(product.getDescription());
            item.setProductSize(product.getSize());
            item.setProductColor(product.getColor());
            if (product.getImages() != null && !product.getImages().isEmpty()) {
                item.setProductImage(product.getImages().get(0).getImageUrl());
            }
            orderItemRepository.save(item);
        }

        // Xóa giỏ hàng sau khi tạo đơn
        cartItemRepository.deleteAll(cartItems);

        return saved;
    }

    @Override
    @Transactional
    public Order updateOrderStatus(Integer id, String status, String cancelReason) {
        Optional<Order> opt = orderRepository.findById(id);
        if (opt.isPresent()) {
            Order order = opt.get();
            String oldStatus = order.getStatus();

            order.setStatus(status);
            if ("Đã hủy".equals(status)) {
                order.setCancelReason(cancelReason);
            } else {
                order.setCancelReason(null);
            }

            // Nếu chuyển từ trạng thái KHÁC "Đã hủy" sang "Đã hủy" => hoàn trả tồn kho
            if (!"Đã hủy".equals(oldStatus) && "Đã hủy".equals(status)) {
                if (order.getOrderItems() != null) {
                    for (OrderItem item : order.getOrderItems()) {
                        if (item == null || item.getProduct() == null) continue;
                        Product p = item.getProduct();
                        Integer cur = p.getQuantity() == null ? 0 : p.getQuantity();
                        Integer inc = item.getQuantity() == null ? 0 : item.getQuantity();
                        p.setQuantity(cur + inc);
                        productRepository.save(p);
                    }
                }
            }

            Order updated = orderRepository.save(order);
            // (Optional) gửi thông báo/email: để nguyên TODO hiện có
            return updated;
        }
        throw new RuntimeException("Không tìm thấy đơn hàng với id " + id);
    }

    @Override
    public void deleteOrder(Integer id) {
        orderRepository.deleteById(id);
    }

    @Override
    public Page<Order> getOrdersByUser(String userCode, Pageable pageable) {
        if (userCode == null || userCode.isBlank()) {
            throw new IllegalArgumentException("userCode không hợp lệ");
        }
        return orderRepository.findByUserCodeOrderByOrderDateDesc(userCode, pageable);
    }
}
