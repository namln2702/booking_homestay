package org.example.do_an_v1.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.do_an_v1.dto.request.CreatePaymentUrlRequest;
import org.example.do_an_v1.dto.request.VNPayPaymentRequest;
import org.example.do_an_v1.dto.request.VNPayReturnRequest;
import org.example.do_an_v1.dto.response.VNPayPaymentResponse;
import org.example.do_an_v1.payload.ApiResponse;
import org.example.do_an_v1.service.VNPayService;
import org.example.do_an_v1.service.support.VNPayPaymentSupport;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/payment")
@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final VNPayService vnPayService;
    private final VNPayPaymentSupport vnPayPaymentSupport;

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
     * Tạo payment URL từ VNPay (API cũ - giữ lại để tương thích)
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
     * Tạo payment URL từ VNPay (API mới - linh hoạt hơn)
     * Có thể truyền billId hoặc transactionId
     * 
     * @param request Thông tin thanh toán (billId hoặc transactionId)
     * @param httpRequest HTTP request để lấy IP address
     * @return Payment URL từ VNPay
     */
//    @PostMapping("/vnpay/create-url")
//    public ApiResponse<VNPayPaymentResponse> createPaymentUrl(
//            @RequestBody @Valid CreatePaymentUrlRequest request,
//            HttpServletRequest httpRequest
//    ) {
//        try {
//            VNPayPaymentResponse response;
//
//            if (request.getTransactionId() != null) {
//                // Tạo payment URL cho transaction cụ thể
//                response = vnPayPaymentSupport.createPaymentUrlForTransactionId(
//                        request.getTransactionId(),
//                        httpRequest
//                );
//            } else if (request.getBillId() != null) {
//                // Tạo payment URL cho bill (tự động tìm transaction PENDING)
//                response = vnPayPaymentSupport.createPaymentUrlForBill(
//                        request.getBillId(),
//                        httpRequest
//                );
//            } else {
//                return new ApiResponse<>(400, "Either billId or transactionId is required", null);
//            }
//
//            return new ApiResponse<>(200, "Payment URL created successfully", response);
//
//        } catch (IllegalArgumentException e) {
//            return new ApiResponse<>(400, e.getMessage(), null);
//        } catch (IllegalStateException e) {
//            return new ApiResponse<>(400, e.getMessage(), null);
//        } catch (Exception e) {
//            return new ApiResponse<>(500, "Error creating payment URL: " + e.getMessage(), null);
//        }
//    }

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
