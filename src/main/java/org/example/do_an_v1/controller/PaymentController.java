package org.example.do_an_v1.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.do_an_v1.dto.request.PaymentNotificationRequest;
import org.example.do_an_v1.dto.request.VNPayPaymentRequest;
import org.example.do_an_v1.dto.request.VNPayReturnRequest;
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
     * Thành công -> chuyển Bill từ DEPOSIT_PENDING sang CHECKIN_PENDING
     * Thất bại -> chuyển Bill sang PAYMENT_FAILED và unlock homestay_daily_prices
     */
//    @PostMapping("/notification")
//    public ApiResponse<?> paymentNotification(@RequestBody @Valid PaymentNotificationRequest request) {
//        return paymentService.handlePaymentNotification(request);
//    }

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
//    @GetMapping("/vnpay/ipn")
//    public ApiResponse<?> vnPayIpnCallback(@RequestParam Map<String, String> params) {
//        return vnPayService.handleIpnCallback(params);
//    }

    /**
     * Xử lý return URL khi user quay lại từ VNPay
     * User sẽ được redirect về endpoint này sau khi thanh toán
     */
//    @GetMapping("/vnpay/return")
//    public ApiResponse<?> vnPayReturn(@RequestParam Map<String, String> params) {
//        return vnPayService.handleReturnUrl(params);
//    }

    /**
     * API xác thực và cập nhật Bill sau khi customer thanh toán
     * FE sẽ gọi API này sau khi nhận thông tin từ VNPay return URL
     * 
     * Luồng:
     * 1. Customer thanh toán xong -> VNPay redirect về FE với thông tin
     * 2. FE lọc thông tin (thành công/thất bại)
     * 3. FE gọi API này để xác thực và cập nhật Bill
     * 
     * @param request Thông tin thanh toán từ FE (các tham số từ VNPay return URL)
     * @return ApiResponse chứa kết quả xác thực và thông tin Bill đã cập nhật
     */
    @PostMapping("/vnpay/verify")
    public ApiResponse<?> verifyPayment(@RequestBody @Valid VNPayReturnRequest request) {
        return vnPayService.verifyAndUpdatePayment(request);
    }
}
