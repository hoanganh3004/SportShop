package com.library.sportshop.service.impl;

import com.library.sportshop.entity.Order;
import com.library.sportshop.entity.OrderItem;
import com.library.sportshop.entity.Product;
import com.library.sportshop.repository.OrderItemRepository;
import com.library.sportshop.repository.OrderRepository;
import com.library.sportshop.repository.ProductRepository;
import com.library.sportshop.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

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
    public Order updateOrderStatus(Integer id, String status, String cancelReason) {
        Optional<Order> opt = orderRepository.findById(id);
        if (opt.isPresent()) {
            Order order = opt.get();
            order.setStatus(status);
            if ("Đã hủy".equals(status)) {
                order.setCancelReason(cancelReason);
            } else {
                order.setCancelReason(null);
            }
            Order updated = orderRepository.save(order);

            // Gửi thông báo + email cho user (TODO: tích hợp service email)
            // emailService.sendOrderStatusEmail(order);

            return updated;
        }
        throw new RuntimeException("Không tìm thấy đơn hàng với id " + id);
    }

    @Override
    public void deleteOrder(Integer id) {
        orderRepository.deleteById(id);
    }


}
