package org.example.do_an_v1.service;

import org.example.do_an_v1.dto.request.PaymentNotificationRequest;
import org.example.do_an_v1.payload.ApiResponse;

public interface PaymentService {
    /**
     * Xử lý thông báo thanh toán từ payment gateway
     * @param request Thông tin thanh toán (billId, transactionId, success)
     * @return ApiResponse thông báo kết quả
     */
    ApiResponse<?> handlePaymentNotification(PaymentNotificationRequest request);
}

