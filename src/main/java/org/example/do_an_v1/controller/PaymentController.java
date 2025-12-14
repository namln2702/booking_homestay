package org.example.do_an_v1.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.do_an_v1.dto.request.VNPayPaymentRequest;
import org.example.do_an_v1.dto.request.VNPayReturnRequest;
import org.example.do_an_v1.payload.ApiResponse;
import org.example.do_an_v1.service.VNPayService;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/payment")
@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final VNPayService vnPayService;

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
     * API xác thực và cập nhật Bill cho thanh toán 30% (deposit)
     * FE sẽ gọi API này sau khi nhận thông tin từ VNPay return URL cho thanh toán deposit
     * 
     * Luồng:
     * 1. Customer thanh toán 30% xong -> VNPay redirect về FE với thông tin
     * 2. FE lọc thông tin (thành công/thất bại)
     * 3. FE gọi API này để xác thực và cập nhật Bill
     * 
     * @param request Thông tin thanh toán từ FE (các tham số từ VNPay return URL)
     * @return ApiResponse chứa kết quả xác thực và thông tin Bill đã cập nhật
     */
    @PostMapping("/vnpay/verify-deposit")
    public ApiResponse<?> verifyDepositPayment(@RequestBody @Valid VNPayReturnRequest request) {
        return vnPayService.verifyDepositPayment(request);
    }

    /**
     * API xác thực và cập nhật Bill cho thanh toán 70% (remaining payment)
     * FE sẽ gọi API này sau khi nhận thông tin từ VNPay return URL cho thanh toán remaining payment
     * 
     * Luồng:
     * 1. Customer thanh toán 70% xong -> VNPay redirect về FE với thông tin
     * 2. FE lọc thông tin (thành công/thất bại)
     * 3. FE gọi API này để xác thực và cập nhật Bill
     * 
     * @param request Thông tin thanh toán từ FE (các tham số từ VNPay return URL)
     * @return ApiResponse chứa kết quả xác thực và thông tin Bill đã cập nhật
     */
    @PostMapping("/vnpay/verify-remaining")
    public ApiResponse<?> verifyRemainingPayment(@RequestBody @Valid VNPayReturnRequest request) {
        return vnPayService.verifyRemainingPayment(request);
    }
}
