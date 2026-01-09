package com.library.sportshop.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_code")
    private String userCode;

    @Column(name = "order_date")
    private LocalDateTime orderDate;

    private String status;

    @Column(name = "cancel_reason")
    private String cancelReason;

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    @Column(name = "recipient_name")
    private String recipientName;

    @Column(name = "recipient_email")
    private String recipientEmail;

    @Column(name = "recipient_phone")
    private String recipientPhone;

    @Column(name = "recipient_address")
    private String recipientAddress;

    @OneToMany(mappedBy = "order")
    private List<OrderItem> orderItems;

    public Order() {
    }

    // Constructor tiện lợi cho tạo đơn hàng mới (id tự generate, cancelReason mặc
    // định null)
    public Order(String userCode, String recipientName, String recipientPhone,
            String recipientAddress, String recipientEmail) {
        this.userCode = userCode;
        this.recipientName = recipientName;
        this.recipientPhone = recipientPhone;
        this.recipientAddress = recipientAddress;
        this.recipientEmail = recipientEmail;
        this.orderDate = LocalDateTime.now();
        this.status = "Chờ xác nhận";
    }

    // Constructor đầy đủ
    public Order(Integer id, String userCode, LocalDateTime orderDate, String status,
            String cancelReason, BigDecimal totalAmount,
            String recipientName, String recipientEmail,
            String recipientPhone, String recipientAddress) {
        this.id = id;
        this.userCode = userCode;
        this.orderDate = orderDate;
        this.status = status;
        this.cancelReason = cancelReason;
        this.totalAmount = totalAmount;
        this.recipientName = recipientName;
        this.recipientEmail = recipientEmail;
        this.recipientPhone = recipientPhone;
        this.recipientAddress = recipientAddress;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUserCode() {
        return userCode;
    }

    public void setUserCode(String userCode) {
        this.userCode = userCode;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    public String getRecipientPhone() {
        return recipientPhone;
    }

    public void setRecipientPhone(String recipientPhone) {
        this.recipientPhone = recipientPhone;
    }

    public String getRecipientAddress() {
        return recipientAddress;
    }

    public void setRecipientAddress(String recipientAddress) {
        this.recipientAddress = recipientAddress;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", userCode='" + userCode + '\'' +
                ", orderDate=" + orderDate +
                ", status='" + status + '\'' +
                ", cancelReason='" + cancelReason + '\'' +
                ", totalAmount=" + totalAmount +
                ", recipientName='" + recipientName + '\'' +
                ", recipientEmail='" + recipientEmail + '\'' +
                ", recipientPhone='" + recipientPhone + '\'' +
                ", recipientAddress='" + recipientAddress + '\'' +
                ", orderItems=" + orderItems +
                '}';
    }
}
