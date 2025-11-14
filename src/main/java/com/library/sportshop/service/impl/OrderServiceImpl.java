package com.library.sportshop.service.impl;

import com.library.sportshop.entity.Order;
import com.library.sportshop.entity.OrderItem;
import com.library.sportshop.entity.OrderStatus;
import com.library.sportshop.entity.Product;
import com.library.sportshop.entity.CartItem;
import com.library.sportshop.entity.Notification;
import com.library.sportshop.repository.OrderItemRepository;
import com.library.sportshop.repository.OrderRepository;
import com.library.sportshop.repository.ProductRepository;
import com.library.sportshop.repository.CartItemRepository;
import com.library.sportshop.service.OrderService;
import com.library.sportshop.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;

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

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String mailFrom;

    @Override
    // lấy danh sách đơn hàng (có tìm kiếm theo tên/email người nhận)
    public Page<Order> getAllOrders(String keyword, Pageable pageable) {
        if (keyword != null && !keyword.isEmpty()) {
            return orderRepository.findByRecipientNameContainingIgnoreCaseOrRecipientEmailContainingIgnoreCase(
                    keyword, keyword, pageable);
        }
        return orderRepository.findAll(pageable);
    }

    @Override
    // lấy đơn hàng theo id
    public Optional<Order> getOrderById(Integer id) {
        return orderRepository.findById(id);
    }

    @Override
    // lưu đơn hàng (mặc định trạng thái Chờ xác nhận nếu chưa có)
    public Order saveOrder(Order order) {
        order.setOrderDate(LocalDateTime.now());
        if (order.getStatus() == null) {
            order.setStatus(OrderStatus.PENDING.getDisplayName());
        }
        return orderRepository.save(order);
    }

    @Override
    @Transactional
    // tạo đơn với 1 sản phẩm cụ thể
    public Order createOrderWithItem(Order order, Integer productId, Integer quantity) {
        order.setOrderDate(LocalDateTime.now());
        if (order.getStatus() == null) {
            order.setStatus(OrderStatus.PENDING.getDisplayName());
        }
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không tồn tại"));

        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Số lượng không hợp lệ");
        }

        // tính đơn giá và tổng tiền
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
    // tạo đơn từ toàn bộ giỏ hàng của người dùng
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

        // kiểm tra tồn kho
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
        order.setStatus(OrderStatus.PENDING.getDisplayName());

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

        // xoá giỏ hàng sau khi tạo đơn
        cartItemRepository.deleteAll(cartItems);

        return saved;
    }

    @Override
    @Transactional
    // cập nhật trạng thái đơn hàng
    public Order updateOrderStatus(Integer id, String status, String cancelReason) {
        Optional<Order> opt = orderRepository.findById(id);
        if (opt.isPresent()) {
            Order order = opt.get();
            String oldStatus = order.getStatus();
            OrderStatus oldStatusEnum = OrderStatus.fromDisplayName(oldStatus);
            OrderStatus newStatusEnum = OrderStatus.fromDisplayName(status);

            order.setStatus(status);
            if (OrderStatus.CANCELLED.getDisplayName().equals(status)) {
                order.setCancelReason(cancelReason);
            } else {
                order.setCancelReason(null);
            }

            // nếu chuyển từ trạng thái khác "Đã hủy" sang "Đã hủy" => hoàn trả tồn kho
            if (oldStatusEnum != OrderStatus.CANCELLED && newStatusEnum == OrderStatus.CANCELLED) {
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

            try {
                String userCode = updated.getUserCode();
                if (userCode != null && !userCode.isBlank()) {
                    Notification n = new Notification();
                    n.setUserCode(userCode);
                    n.setMessage("Trạng thái đơn hàng #" + updated.getId() + " đã cập nhật: " + status);
                    notificationService.saveNotification(n);
                }
            } catch (Exception ignored) {}

            try {
                String to = updated.getRecipientEmail();
                if (to != null && !to.isBlank()) {
                    MimeMessage mimeMessage = mailSender.createMimeMessage();
                    MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
                    helper.setTo(to);
                    helper.setSubject("Cập nhật đơn hàng - SportShop");
                    helper.setText("Đơn hàng #" + updated.getId() + " đã được cập nhật trạng thái: " + status, false);
                    if (mailFrom != null && !mailFrom.isBlank()) {
                        helper.setFrom(new InternetAddress(mailFrom, "SportShop"));
                    }
                    mailSender.send(mimeMessage);
                }
            } catch (Exception ignored) {}
            return updated;
        }
        throw new RuntimeException("Không tìm thấy đơn hàng với id " + id);
    }

    @Override
    // xoá đơn hàng theo id
    public void deleteOrder(Integer id) {
        orderRepository.deleteById(id);
    }

    @Override
    // lấy danh sách đơn hàng theo user (mới nhất trước)
    public Page<Order> getOrdersByUser(String userCode, Pageable pageable) {
        if (userCode == null || userCode.isBlank()) {
            throw new IllegalArgumentException("userCode không hợp lệ");
        }
        return orderRepository.findByUserCodeOrderByOrderDateDesc(userCode, pageable);
    }
}
