package org.example.do_an_v1.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.do_an_v1.configuration.VNPayConfig;
import org.example.do_an_v1.dto.request.VNPayPaymentRequest;
import org.example.do_an_v1.dto.request.VNPayReturnRequest;
import org.example.do_an_v1.dto.response.VNPayPaymentResponse;
import org.example.do_an_v1.entity.Bill;
import org.example.do_an_v1.entity.HomestayDailyPrice;
import org.example.do_an_v1.entity.Transaction;
import org.example.do_an_v1.enums.StatusBill;
import org.example.do_an_v1.enums.StatusTransaction;
import org.example.do_an_v1.payload.ApiResponse;
import org.example.do_an_v1.repository.BillRepository;
import org.example.do_an_v1.repository.HomestayDailyPricesRepository;
import org.example.do_an_v1.repository.TransactionRepository;
import org.example.do_an_v1.service.EmailService;
import org.example.do_an_v1.service.VNPayService;
import org.example.do_an_v1.service.support.VNPayPaymentHandler;
import org.example.do_an_v1.service.support.VNPayPaymentSupport;
import org.example.do_an_v1.service.support.VNPayVerifyResult;
import org.example.do_an_v1.service.support.VNPayVerifySupport;
import org.example.do_an_v1.utils.VNPayUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class VNPayServiceImpl implements VNPayService {

    private final VNPayConfig vnPayConfig;
    private final BillRepository billRepository;
    private final TransactionRepository transactionRepository;
    private final HomestayDailyPricesRepository homestayDailyPricesRepository;
    private final EmailService emailService;
    private final VNPayVerifySupport vnPayVerifySupport;
    private final VNPayPaymentSupport vnPayPaymentSupport;
    private final VNPayPaymentHandler vnPayPaymentHandler;

    @Override
    @Transactional
    public ApiResponse<VNPayPaymentResponse> createPaymentUrl(VNPayPaymentRequest request, HttpServletRequest httpRequest) {
        // Sử dụng component hỗ trợ để tạo payment URL
        VNPayPaymentResponse response = vnPayPaymentSupport.createPaymentUrlForBill(
                request.getBillId(),
                httpRequest
        );

        if (response == null) {
            return new ApiResponse<>(400, "Failed to create payment URL. Please check bill and transaction status.", null);
        }

        return new ApiResponse<>(200, "Payment URL created successfully", response);
    }

    @Override
    @Transactional
    public ApiResponse<?> handleIpnCallback(Map<String, String> params) {
        try {
            log.info("Received VNPay IPN callback: {}", params);

            // Xác thực chữ ký
            if (!VNPayUtil.verifyPayment(params, vnPayConfig.getSecretKey())) {
                log.warn("Invalid VNPay IPN signature");
                return new ApiResponse<>(400, "Invalid signature", null);
            }

            // Lấy thông tin từ params
            String vnp_ResponseCode = params.get("vnp_ResponseCode");
            String vnp_TxnRef = params.get("vnp_TxnRef");
            String vnp_TransactionStatus = params.get("vnp_TransactionStatus");
            String vnp_Amount = params.get("vnp_Amount");
            // Có thể sử dụng các thông tin sau để log hoặc lưu vào database nếu cần
            // String vnp_TransactionNo = params.get("vnp_TransactionNo");
            // String vnp_BankCode = params.get("vnp_BankCode");
            // String vnp_PayDate = params.get("vnp_PayDate");

            // Tra cứu transaction bằng orderId (vnp_TxnRef)
            Transaction transaction = transactionRepository.findByOrderId(vnp_TxnRef).orElse(null);
            if (transaction == null) {
                return new ApiResponse<>(404, "Transaction not found with orderId: " + vnp_TxnRef, null);
            }

            Bill bill = transaction.getBill();

            // Kiểm tra amount
            long amountInVnd = Long.parseLong(vnp_Amount) / 100; // VNPay trả về amount tính bằng xu
            if (amountInVnd != transaction.getAmount().longValue()) {
                log.error("Amount mismatch. Expected: {}, Received: {}", transaction.getAmount(), amountInVnd);
                return new ApiResponse<>(400, "Amount mismatch", null);
            }

            // Xử lý kết quả thanh toán
            if ("00".equals(vnp_ResponseCode) && "00".equals(vnp_TransactionStatus)) {
                // Thanh toán thành công
                if (bill.getStatus() == StatusBill.DEPOSIT_PENDING) {
                    bill.setStatus(StatusBill.CHECKIN_PENDING);
                    transaction.setStatus(StatusTransaction.SUCCESS);
                    
                    billRepository.save(bill);
                    transactionRepository.save(transaction);

                    // Gửi email mã code bill cho customer nếu có email
                    if (bill.getCustomer() != null
                            && bill.getCustomer().getUser() != null
                            && bill.getCustomer().getUser().getEmail() != null) {
                        String email = bill.getCustomer().getUser().getEmail();
                        String code = bill.getCode();
                        if (code != null && !code.isBlank()) {
                            emailService.sendSimpleEmail(email, "Your booking code: " + code);
                        }
                    }

                    log.info("Payment successful for bill {}: orderId {}", bill.getId(), vnp_TxnRef);
                    return new ApiResponse<>(200, "Payment successful", null);
                } else if (bill.getStatus() == StatusBill.REMAINING_PAYMENT_PENDING) {
                    // Thanh toán phần còn lại thành công -> chuyển sang COMPLAINT_PENDING
                    bill.setStatus(StatusBill.COMPLAINT_PENDING);
                    transaction.setStatus(StatusTransaction.SUCCESS);
                    
                    billRepository.save(bill);
                    transactionRepository.save(transaction);

                    log.info("Remaining payment successful for bill {}: orderId {}", bill.getId(), vnp_TxnRef);
                    return new ApiResponse<>(200, "Remaining payment successful", null);
                } else {
                    log.warn("Bill {} is not in DEPOSIT_PENDING or REMAINING_PAYMENT_PENDING status. Current status: {}", bill.getId(), bill.getStatus());
                    return new ApiResponse<>(200, "Payment already processed", null);
                }
            } else {
                // Thanh toán thất bại - unlock homestay_daily_prices (chỉ khi thanh toán cọc thất bại)
                if (bill.getStatus() == StatusBill.DEPOSIT_PENDING) {
                    // Unlock homestay_daily_prices
                    List<HomestayDailyPrice> dailyPrices = homestayDailyPricesRepository.findAll().stream()
                            .filter(hdp -> hdp.getBill() != null && hdp.getBill().getId().equals(bill.getId()))
                            .toList();

                    for (HomestayDailyPrice dailyPrice : dailyPrices) {
                        dailyPrice.setIsBooked(false);
                        dailyPrice.setBill(null);
                        homestayDailyPricesRepository.save(dailyPrice);
                    }

                    bill.setStatus(StatusBill.PAYMENT_FAILED);
                    transaction.setStatus(StatusTransaction.FAILED);
                    
                    billRepository.save(bill);
                    transactionRepository.save(transaction);

                    log.info("Payment failed for bill {}: orderId {}. Daily prices unlocked.", bill.getId(), vnp_TxnRef);
                    return new ApiResponse<>(200, "Payment failed", null);
                }
            }

            return new ApiResponse<>(200, "IPN processed", null);

        } catch (Exception e) {
            log.error("Error processing VNPay IPN callback", e);
            return new ApiResponse<>(500, "Error processing IPN: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<?> handleReturnUrl(Map<String, String> params) {
        try {
            log.info("Received VNPay return URL: {}", params);

            // Xác thực chữ ký
            if (!VNPayUtil.verifyPayment(params, vnPayConfig.getSecretKey())) {
                log.warn("Invalid VNPay return URL signature");
                return new ApiResponse<>(400, "Invalid signature", null);
            }

            // Lấy thông tin từ params
            String vnp_ResponseCode = params.get("vnp_ResponseCode");
            String vnp_TxnRef = params.get("vnp_TxnRef");
            String vnp_TransactionStatus = params.get("vnp_TransactionStatus");

            // Tra cứu transaction bằng orderId (vnp_TxnRef)
            Optional<Transaction> transactionOpt = transactionRepository.findByOrderId(vnp_TxnRef);
            
            if (transactionOpt.isEmpty()) {
                return new ApiResponse<>(404, "Transaction not found with orderId: " + vnp_TxnRef, null);
            }

            Transaction transaction = transactionOpt.get();
            Bill bill = transaction.getBill();

            // Trả về thông tin để frontend xử lý
            Map<String, Object> result = Map.of(
                    "success", "00".equals(vnp_ResponseCode) && "00".equals(vnp_TransactionStatus),
                    "billId", bill.getId(),
                    "billCode", bill.getCode(),
                    "transactionId", transaction.getId(),
                    "orderId", vnp_TxnRef,
                    "responseCode", vnp_ResponseCode,
                    "message", "00".equals(vnp_ResponseCode) ? "Payment successful" : "Payment failed"
            );

            return new ApiResponse<>(200, "Return URL processed", result);

        } catch (Exception e) {
            log.error("Error processing VNPay return URL", e);
            return new ApiResponse<>(500, "Error processing return URL: " + e.getMessage(), null);
        }
    }


    @Override
    @Transactional
    public ApiResponse<?> verifyAndUpdatePayment(VNPayReturnRequest request) {
        // Bước 1: Verify signature và validate amount (chỉ verify, không xử lý nghiệp vụ)
        VNPayVerifyResult verifyResult = vnPayVerifySupport.verifyPayment(request);
        
        // Kiểm tra nếu verifyResult có error (transaction not found, missing params, etc.)
        if (verifyResult.getErrorMessage() != null && !verifyResult.isVerifySuccess()) {
            return new ApiResponse<>(400, verifyResult.getErrorMessage(), null);
        }
        
        // Bước 2: Xử lý nghiệp vụ dựa trên bill status
        Map<String, Object> result;
        Bill bill = verifyResult.getBill();
        
        if (bill == null) {
            return new ApiResponse<>(400, "Bill not found", null);
        }
        
        if (bill.getStatus() == StatusBill.DEPOSIT_PENDING) {
            // Xử lý verify cho thanh toán 30% đầu tiên
            result = vnPayPaymentHandler.handleDepositPayment(verifyResult);
        } else if (bill.getStatus() == StatusBill.REMAINING_PAYMENT_PENDING) {
            // Xử lý verify cho thanh toán 70% còn lại
            result = vnPayPaymentHandler.handleRemainingPayment(verifyResult);
        } else {
            log.warn("Bill {} is not in DEPOSIT_PENDING or REMAINING_PAYMENT_PENDING status. Current status: {}", 
                    bill.getId(), bill.getStatus());
            return new ApiResponse<>(400, 
                    "Bill is not in DEPOSIT_PENDING or REMAINING_PAYMENT_PENDING status. Current status: " + bill.getStatus(), 
                    null);
        }
        
        String message = (String) result.get("message");
        
        return new ApiResponse<>(200, message, result);
    }
}

