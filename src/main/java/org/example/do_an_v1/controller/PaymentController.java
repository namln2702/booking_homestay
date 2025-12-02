package org.example.do_an_v1.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.do_an_v1.dto.request.PaymentNotificationRequest;
import org.example.do_an_v1.payload.ApiResponse;
import org.example.do_an_v1.service.PaymentService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/payment")
@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * API nhận thông báo về quá trình thanh toán từ payment gateway
     * Thành công -> chuyển Bill sang CHECKIN_PENDING
     * Thất bại -> chuyển Bill sang PAYMENT_FAILED và unlock homestay_daily_prices
     */
    @PostMapping("/notification")
    public ApiResponse<?> paymentNotification(@RequestBody @Valid PaymentNotificationRequest request) {
        return paymentService.handlePaymentNotification(request);
    }
}
