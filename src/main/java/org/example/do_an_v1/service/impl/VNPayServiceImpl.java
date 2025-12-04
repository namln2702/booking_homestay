package org.example.do_an_v1.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.do_an_v1.configuration.VNPayConfig;
import org.example.do_an_v1.dto.request.VNPayPaymentRequest;
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

    @Override
    @Transactional
    public ApiResponse<VNPayPaymentResponse> createPaymentUrl(VNPayPaymentRequest request, HttpServletRequest httpRequest) {
        try {
            // Tìm bill
            Bill bill = billRepository.findById(request.getBillId())
                    .orElseThrow(() -> new RuntimeException("Bill not found with id: " + request.getBillId()));

            // Validate: Bill phải ở trạng thái PAYMENT_PENDING
            if (bill.getStatus() != StatusBill.PAYMENT_PENDING) {
                return new ApiResponse<>(400, "Bill must be in PAYMENT_PENDING status. Current status: " + bill.getStatus(), null);
            }

            // Tìm transaction
            Transaction transaction = transactionRepository.findByBillId(bill.getId())
                    .orElseThrow(() -> new RuntimeException("Transaction not found for bill id: " + bill.getId()));

            // Validate: Transaction phải ở trạng thái PENDING
            if (transaction.getStatus() != StatusTransaction.PENDING) {
                return new ApiResponse<>(400, "Transaction must be in PENDING status. Current status: " + transaction.getStatus(), null);
            }

            // Tạo orderId từ bill code và transaction id
            String orderId = bill.getCode() + "_" + transaction.getId();
            
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

            // Parse orderId để lấy bill code và transaction id
            // Format: {billCode}_{transactionId}
            String[] orderParts = vnp_TxnRef.split("_");
            if (orderParts.length < 2) {
                log.error("Invalid orderId format: {}", vnp_TxnRef);
                return new ApiResponse<>(400, "Invalid orderId format", null);
            }

            Long transactionId = Long.parseLong(orderParts[orderParts.length - 1]);
            Transaction transaction = transactionRepository.findById(transactionId)
                    .orElseThrow(() -> new RuntimeException("Transaction not found with id: " + transactionId));

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
                if (bill.getStatus() == StatusBill.PAYMENT_PENDING) {
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

                    log.info("Payment successful for bill {}: transaction {}", bill.getId(), transactionId);
                    return new ApiResponse<>(200, "Payment successful", null);
                } else {
                    log.warn("Bill {} is not in PAYMENT_PENDING status. Current status: {}", bill.getId(), bill.getStatus());
                    return new ApiResponse<>(200, "Payment already processed", null);
                }
            } else {
                // Thanh toán thất bại - unlock homestay_daily_prices
                if (bill.getStatus() == StatusBill.PAYMENT_PENDING) {
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

                    log.info("Payment failed for bill {}: transaction {}. Daily prices unlocked.", bill.getId(), transactionId);
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

            // Parse orderId để lấy transaction id
            String[] orderParts = vnp_TxnRef.split("_");
            if (orderParts.length < 2) {
                log.error("Invalid orderId format: {}", vnp_TxnRef);
                return new ApiResponse<>(400, "Invalid orderId format", null);
            }

            Long transactionId = Long.parseLong(orderParts[orderParts.length - 1]);
            Optional<Transaction> transactionOpt = transactionRepository.findById(transactionId);
            
            if (transactionOpt.isEmpty()) {
                return new ApiResponse<>(404, "Transaction not found", null);
            }

            Transaction transaction = transactionOpt.get();
            Bill bill = transaction.getBill();

            // Trả về thông tin để frontend xử lý
            Map<String, Object> result = Map.of(
                    "success", "00".equals(vnp_ResponseCode) && "00".equals(vnp_TransactionStatus),
                    "billId", bill.getId(),
                    "billCode", bill.getCode(),
                    "transactionId", transactionId,
                    "responseCode", vnp_ResponseCode,
                    "message", "00".equals(vnp_ResponseCode) ? "Payment successful" : "Payment failed"
            );

            return new ApiResponse<>(200, "Return URL processed", result);

        } catch (Exception e) {
            log.error("Error processing VNPay return URL", e);
            return new ApiResponse<>(500, "Error processing return URL: " + e.getMessage(), null);
        }
    }
}

