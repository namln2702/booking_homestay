package org.example.do_an_v1.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.do_an_v1.dto.request.PaymentNotificationRequest;
import org.example.do_an_v1.dto.request.VNPayPaymentRequest;
import org.example.do_an_v1.payload.ApiResponse;
import org.example.do_an_v1.service.PaymentService;
import org.example.do_an_v1.service.VNPayService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequestMapping("/payment")
@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final VNPayService vnPayService;

    /**
     * API nhận thông báo về quá trình thanh toán từ payment gateway
     * Thành công -> chuyển Bill sang CHECKIN_PENDING
     * Thất bại -> chuyển Bill sang PAYMENT_FAILED và unlock homestay_daily_prices
     */
    @PostMapping("/notification")
    public ApiResponse<?> paymentNotification(@RequestBody @Valid PaymentNotificationRequest request) {
        return paymentService.handlePaymentNotification(request);
    }

    /**
     * Tạo payment URL từ VNPay
     * @param request Thông tin thanh toán (billId)
     * @param httpRequest HTTP request để lấy IP address
     * @return Payment URL từ VNPay
     */
    @PostMapping("/vnpay/create")
    public ApiResponse<?> createVNPayPayment(@RequestBody @Valid VNPayPaymentRequest request,
                                            HttpServletRequest httpRequest) {
        return vnPayService.createPaymentUrl(request, httpRequest);
    }

    /**
     * Xử lý IPN callback từ VNPay
     * VNPay sẽ gọi endpoint này để thông báo kết quả thanh toán
     */
    @GetMapping("/vnpay/ipn")
    public ApiResponse<?> vnPayIpnCallback(@RequestParam Map<String, String> params) {
        return vnPayService.handleIpnCallback(params);
    }

    /**
     * Xử lý return URL khi user quay lại từ VNPay
     * User sẽ được redirect về endpoint này sau khi thanh toán
     */
    @GetMapping("/vnpay/return")
    public ApiResponse<?> vnPayReturn(@RequestParam Map<String, String> params) {
        return vnPayService.handleReturnUrl(params);
    }
}
