package com.library.sportshop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class ErrorController implements org.springframework.boot.web.servlet.error.ErrorController {

    @GetMapping("/403")
    public String errorController() {
        return "403";
    }
    
    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Integer statusCode = (Integer) request.getAttribute("jakarta.servlet.error.status_code");
        String errorMessage = (String) request.getAttribute("jakarta.servlet.error.message");
        Exception exception = (Exception) request.getAttribute("jakarta.servlet.error.exception");
        
        model.addAttribute("statusCode", statusCode);
        model.addAttribute("errorMessage", errorMessage);
        
        if (statusCode != null) {
            switch (statusCode) {
                case 400:
                    model.addAttribute("error", "Yêu cầu không hợp lệ. Vui lòng kiểm tra dữ liệu đầu vào.");
                    break;
                case 404:
                    model.addAttribute("error", "Không tìm thấy trang yêu cầu.");
                    break;
                case 500:
                    model.addAttribute("error", "Lỗi server nội bộ. Vui lòng thử lại sau.");
                    break;
                default:
                    model.addAttribute("error", "Đã xảy ra lỗi: " + errorMessage);
            }
        }
        
        // Log lỗi để debug
        System.err.println("Error occurred - Status: " + statusCode + ", Message: " + errorMessage);
        if (exception != null) {
            exception.printStackTrace();
        }
        
        return "error";
    }
}