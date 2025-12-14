package org.example.do_an_v1.service;

import jakarta.servlet.http.HttpServletRequest;
import org.example.do_an_v1.dto.request.VNPayPaymentRequest;
import org.example.do_an_v1.dto.request.VNPayReturnRequest;
import org.example.do_an_v1.dto.response.VNPayPaymentResponse;
import org.example.do_an_v1.payload.ApiResponse;

public interface VNPayService {
    /**
     * Tạo payment URL từ VNPay
     * @param request Thông tin thanh toán (billId)
     * @param httpRequest HTTP request để lấy IP address
     * @return ApiResponse chứa payment URL
     */
    ApiResponse<VNPayPaymentResponse> createPaymentUrl(VNPayPaymentRequest request, HttpServletRequest httpRequest);

    /**
     * Xác thực và cập nhật Bill cho thanh toán 30% (deposit)
     * API này được FE gọi sau khi nhận thông tin từ VNPay return URL
     * @param request Thông tin thanh toán từ FE
     * @return ApiResponse thông báo kết quả xác thực và cập nhật
     */
    ApiResponse<?> verifyDepositPayment(VNPayReturnRequest request);

    /**
     * Xác thực và cập nhật Bill cho thanh toán 70% (remaining payment)
     * API này được FE gọi sau khi nhận thông tin từ VNPay return URL
     * @param request Thông tin thanh toán từ FE
     * @return ApiResponse thông báo kết quả xác thực và cập nhật
     */
    ApiResponse<?> verifyRemainingPayment(VNPayReturnRequest request);
}

