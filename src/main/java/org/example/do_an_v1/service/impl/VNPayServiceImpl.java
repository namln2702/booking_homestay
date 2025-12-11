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
import org.example.do_an_v1.utils.GenNumber;
import org.example.do_an_v1.utils.VNPayUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
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

    @Override
    @Transactional
    public ApiResponse<VNPayPaymentResponse> createPaymentUrl(VNPayPaymentRequest request, HttpServletRequest httpRequest) {
        try {
            // Tìm bill
            Bill bill = billRepository.findById(request.getBillId()).orElse(null);
            if (bill == null) {
                return new ApiResponse<>(404, "Bill not found with id: " + request.getBillId(), null);
            }

            // Validate: Bill phải ở trạng thái DEPOSIT_PENDING hoặc REMAINING_PAYMENT_PENDING
            if (bill.getStatus() != StatusBill.DEPOSIT_PENDING && bill.getStatus() != StatusBill.REMAINING_PAYMENT_PENDING) {
                return new ApiResponse<>(400, "Bill must be in DEPOSIT_PENDING or REMAINING_PAYMENT_PENDING status. Current status: " + bill.getStatus(), null);
            }

            // Tìm transaction PENDING (có thể là transaction cọc hoặc transaction phần còn lại)
            Transaction transaction = transactionRepository.findByBillId(bill.getId()).stream()
                    .filter(t -> t.getStatus() == StatusTransaction.PENDING)
                    .findFirst()
                    .orElse(null);
            
            if (transaction == null) {
                return new ApiResponse<>(404, "Pending transaction not found for bill id: " + bill.getId(), null);
            }

            // Validate: Transaction phải ở trạng thái PENDING
            if (transaction.getStatus() != StatusTransaction.PENDING) {
                return new ApiResponse<>(400, "Transaction must be in PENDING status. Current status: " + transaction.getStatus(), null);
            }

            // Tạo orderId chỉ là số (8 chữ số) - VNPay yêu cầu vnp_TxnRef chỉ chứa số
            String orderId = GenNumber.generateVNPayOrderId();
            
            // Lưu orderId vào transaction để tra cứu khi callback
            transaction.setOrderId(orderId);
            transactionRepository.save(transaction);
            
            // Tạo orderInfo
            String orderInfo = "Thanh toan don hang " + bill.getCode();
            
            // Lấy amount từ transaction (đã tính bằng VND)
            long amount = transaction.getAmount().longValue();
            
            // Lấy IP address
            String ipAddress = VNPayUtil.getIpAddress(httpRequest);

            // Tạo payment URL
            String paymentUrl = VNPayUtil.createPaymentUrl(
                    vnPayConfig,
                    orderId,
                    amount,
                    orderInfo,
                    ipAddress
            );

            VNPayPaymentResponse response = VNPayPaymentResponse.builder()
                    .paymentUrl(paymentUrl)
                    .orderId(orderId)
                    .amount(amount)
                    .message("Payment URL created successfully")
                    .build();

            log.info("Created VNPay payment URL for bill {}: {}", bill.getId(), orderId);
            return new ApiResponse<>(200, "Payment URL created successfully", response);

        } catch (Exception e) {
            log.error("Error creating VNPay payment URL for bill {}", request.getBillId(), e);
            return new ApiResponse<>(500, "Error creating payment URL: " + e.getMessage(), null);
        }
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

    /**
     * Helper method: Unlock homestay daily prices và update bill/transaction khi có lỗi
     */
    private void handlePaymentFailure(Bill bill, Transaction transaction, String reason) {
        if (bill == null || transaction == null) {
            log.warn("Cannot handle payment failure: bill or transaction is null");
            return;
        }

        // Chỉ xử lý nếu bill đang ở trạng thái DEPOSIT_PENDING
        if (bill.getStatus() != StatusBill.DEPOSIT_PENDING) {
            log.warn("Bill {} is not in DEPOSIT_PENDING status. Current status: {}. Reason: {}", 
                    bill.getId(), bill.getStatus(), reason);
            return;
        }

        // Unlock homestay_daily_prices
        List<HomestayDailyPrice> dailyPrices = homestayDailyPricesRepository.findAll().stream()
                .filter(hdp -> hdp.getBill() != null && hdp.getBill().getId().equals(bill.getId()))
                .toList();

        for (HomestayDailyPrice dailyPrice : dailyPrices) {
            dailyPrice.setIsBooked(false);
            dailyPrice.setBill(null);
            homestayDailyPricesRepository.save(dailyPrice);
        }

        // Update bill và transaction status
        bill.setStatus(StatusBill.PAYMENT_FAILED);
        transaction.setStatus(StatusTransaction.FAILED);
        
        billRepository.save(bill);
        transactionRepository.save(transaction);

        log.info("Payment failed for bill {}: {}. Daily prices unlocked.", bill.getId(), reason);
    }

    @Override
    @Transactional
    public ApiResponse<?> verifyAndUpdatePayment(VNPayReturnRequest request) {
        Transaction transaction = null;
        Bill bill = null;
        
        try {
            log.info("Received payment verification request from FE: orderId={}, responseCode={}", 
                    request.getVnp_TxnRef(), request.getVnp_ResponseCode());

            // Tra cứu transaction bằng orderId trước (để có thể update khi có lỗi)
            Optional<Transaction> transactionOpt = transactionRepository.findByOrderId(request.getVnp_TxnRef());
            if (transactionOpt.isEmpty()) {
                log.error("Transaction not found with orderId: {}", request.getVnp_TxnRef());
                return new ApiResponse<>(404, "Transaction not found with orderId: " + request.getVnp_TxnRef(), null);
            }
            
            transaction = transactionOpt.get();
            bill = transaction.getBill();

            // Convert request thành Map để verify signature
            // Phải bao gồm TẤT CẢ các tham số từ VNPay (trừ vnp_SecureHash và vnp_SecureHashType)
            // để hashData khớp với hashData mà VNPay đã tính
            Map<String, String> params = new HashMap<>();
            
            // Các tham số BẮT BUỘC
            params.put("vnp_TmnCode", request.getVnp_TmnCode());
            params.put("vnp_TxnRef", request.getVnp_TxnRef());
            params.put("vnp_ResponseCode", request.getVnp_ResponseCode());
            params.put("vnp_TransactionStatus", request.getVnp_TransactionStatus());
            params.put("vnp_Amount", request.getVnp_Amount());
            params.put("vnp_BankCode", request.getVnp_BankCode());
            params.put("vnp_OrderInfo", request.getVnp_OrderInfo());
            params.put("vnp_TransactionNo", request.getVnp_TransactionNo());
            params.put("vnp_SecureHash", request.getVnp_SecureHash());
            
            // Các tham số TÙY CHỌN - chỉ thêm nếu có giá trị
            if (request.getVnp_BankTranNo() != null && !request.getVnp_BankTranNo().isEmpty()) {
                params.put("vnp_BankTranNo", request.getVnp_BankTranNo());
            }
            if (request.getVnp_CardType() != null && !request.getVnp_CardType().isEmpty()) {
                params.put("vnp_CardType", request.getVnp_CardType());
            }
            if (request.getVnp_PayDate() != null && !request.getVnp_PayDate().isEmpty()) {
                params.put("vnp_PayDate", request.getVnp_PayDate());
            }

            // Xác thực chữ ký
            log.debug("Verifying payment signature for orderId: {}, params: {}", 
                    request.getVnp_TxnRef(), params);
            
            boolean isValid = VNPayUtil.verifyPayment(params, vnPayConfig.getSecretKey());
            
            if (!isValid) {
                log.warn("Invalid VNPay signature for orderId: {}. Params received: {}", 
                        request.getVnp_TxnRef(), params);
                // Update bill và transaction khi signature không hợp lệ
                handlePaymentFailure(bill, transaction, "Invalid signature");
                return new ApiResponse<>(400, "Invalid signature", null);
            }
            
            log.info("Payment signature verified successfully for orderId: {}", request.getVnp_TxnRef());

            // Kiểm tra amount
            long amountInVnd = Long.parseLong(request.getVnp_Amount()) / 100; // VNPay trả về amount tính bằng xu
            if (amountInVnd != transaction.getAmount().longValue()) {
                log.error("Amount mismatch. Expected: {}, Received: {}", transaction.getAmount(), amountInVnd);
                // Update bill và transaction khi amount không khớp
                handlePaymentFailure(bill, transaction, "Amount mismatch");
                return new ApiResponse<>(400, "Amount mismatch", null);
            }

            // Xử lý kết quả thanh toán
            boolean isSuccess = "00".equals(request.getVnp_ResponseCode()) 
                    && "00".equals(request.getVnp_TransactionStatus());

            if (isSuccess) {
                // Thanh toán thành công
                if (bill.getStatus() == StatusBill.DEPOSIT_PENDING) {
                    // Thanh toán cọc thành công -> chuyển sang CHECKIN_PENDING
                    bill.setStatus(StatusBill.CHECKIN_PENDING);
                    transaction.setStatus(StatusTransaction.SUCCESS);
                } else if (bill.getStatus() == StatusBill.REMAINING_PAYMENT_PENDING) {
                    // Thanh toán phần còn lại thành công -> chuyển sang COMPLAINT_PENDING
                    bill.setStatus(StatusBill.COMPLAINT_PENDING);
                    transaction.setStatus(StatusTransaction.SUCCESS);
                } else {
                    log.warn("Bill {} is not in DEPOSIT_PENDING or REMAINING_PAYMENT_PENDING status. Current status: {}", 
                            bill.getId(), bill.getStatus());
                    return new ApiResponse<>(400, "Bill is not in DEPOSIT_PENDING or REMAINING_PAYMENT_PENDING status. Current status: " + bill.getStatus(), null);
                }
                    
                    billRepository.save(bill);
                    transactionRepository.save(transaction);

                    // Gửi email mã code bill cho customer nếu có email
                    if (bill.getCustomer() != null
                            && bill.getCustomer().getUser() != null
                            && bill.getCustomer().getUser().getEmail() != null) {
                        String email = bill.getCustomer().getUser().getEmail();
                        String code = bill.getCode();
                        String name = bill.getCustomer().getUser().getName();
                        Long billId = bill.getId();

                        if (code != null && !code.isBlank()) {
                            String content = String.format(
                                    "Hello %s,\n\n" +
                                            "Thank you for your booking.\n\n" +
                                            "Booking information:\n" +
                                            "- Booking code: %s\n" +
                                            "- Bill ID: %d\n\n" +
                                            "Please keep this information for your reference.\n\n" +
                                            "Best regards,\n" +
                                            "Homestay Booking System",
                                    name,
                                    code,
                                    billId
                            );

                            emailService.sendSimpleEmail(email, content);
                        }
                    }

                    log.info("Payment verified and updated successfully for bill {}: orderId {}", 
                            bill.getId(), request.getVnp_TxnRef());
                    
                    Map<String, Object> result = Map.of(
                            "success", true,
                            "billId", bill.getId(),
                            "billCode", bill.getCode(),
                            "transactionId", transaction.getId(),
                            "orderId", request.getVnp_TxnRef(),
                            "message", "Payment verified and updated successfully"
                    );
                    
                    return new ApiResponse<>(200, "Payment verified and updated successfully", result);
            } else {
                // Thanh toán thất bại - unlock homestay_daily_prices
                handlePaymentFailure(bill, transaction, "Payment failed: responseCode=" + request.getVnp_ResponseCode());
                
                Map<String, Object> result = Map.of(
                        "success", false,
                        "billId", bill.getId(),
                        "billCode", bill.getCode(),
                        "transactionId", transaction.getId(),
                        "orderId", request.getVnp_TxnRef(),
                        "responseCode", request.getVnp_ResponseCode(),
                        "message", "Payment failed"
                );
                
                return new ApiResponse<>(200, "Payment failed", result);
            }

        } catch (Exception e) {
            log.error("Error verifying and updating payment for orderId: {}", 
                    request.getVnp_TxnRef(), e);
            
            // Update bill và transaction khi có exception
            if (bill != null && transaction != null) {
                handlePaymentFailure(bill, transaction, "Exception: " + e.getMessage());
            }
            
            return new ApiResponse<>(500, "Error verifying payment: " + e.getMessage(), null);
        }
    }
}

