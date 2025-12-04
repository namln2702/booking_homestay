package org.example.do_an_v1.service;

import jakarta.servlet.http.HttpServletRequest;
import org.example.do_an_v1.dto.request.VNPayPaymentRequest;
import org.example.do_an_v1.dto.response.VNPayPaymentResponse;
import org.example.do_an_v1.payload.ApiResponse;

import java.util.Map;

public interface VNPayService {
    /**
     * Tạo payment URL từ VNPay
     * @param request Thông tin thanh toán (billId)
     * @param httpRequest HTTP request để lấy IP address
     * @return ApiResponse chứa payment URL
     */
    ApiResponse<VNPayPaymentResponse> createPaymentUrl(VNPayPaymentRequest request, HttpServletRequest httpRequest);

    /**
     * Xử lý IPN callback từ VNPay
     * @param params Các tham số từ VNPay callback
     * @return ApiResponse thông báo kết quả
     */
    ApiResponse<?> handleIpnCallback(Map<String, String> params);

    /**
     * Xử lý return URL khi user quay lại từ VNPay
     * @param params Các tham số từ VNPay return URL
     * @return ApiResponse thông báo kết quả
     */
    ApiResponse<?> handleReturnUrl(Map<String, String> params);
}

