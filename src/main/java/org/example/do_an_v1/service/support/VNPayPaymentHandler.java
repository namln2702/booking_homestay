package org.example.do_an_v1.service.support;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.do_an_v1.entity.Bill;
import org.example.do_an_v1.entity.HomestayDailyPrice;
import org.example.do_an_v1.entity.Transaction;
import org.example.do_an_v1.enums.StatusBill;
import org.example.do_an_v1.enums.StatusTransaction;
import org.example.do_an_v1.repository.BillRepository;
import org.example.do_an_v1.repository.HomestayDailyPricesRepository;
import org.example.do_an_v1.repository.TransactionRepository;
import org.example.do_an_v1.service.EmailService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Component xử lý nghiệp vụ sau khi verify payment thành công
 * Xử lý riêng cho từng loại transaction (deposit 30%, remaining payment 70%, v.v.)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VNPayPaymentHandler {

    private final BillRepository billRepository;
    private final TransactionRepository transactionRepository;
    private final HomestayDailyPricesRepository homestayDailyPricesRepository;
    private final EmailService emailService;

    /**
     * Xử lý verify cho thanh toán 30% đầu tiên (DEPOSIT_PENDING)
     * 
     * @param verifyResult Kết quả verify từ VNPayVerifySupport
     * @return Map chứa kết quả xử lý
     */
    @Transactional
    public Map<String, Object> handleDepositPayment(VNPayVerifyResult verifyResult) {
        if (!verifyResult.isVerifySuccess()) {
            // Nếu verify không thành công, xử lý failure
            return handleDepositPaymentFailure(verifyResult);
        }

        if (!verifyResult.isPaymentSuccess()) {
            // Nếu payment thất bại, xử lý failure
            return handleDepositPaymentFailure(verifyResult);
        }

        Bill bill = verifyResult.getBill();
        Transaction transaction = verifyResult.getTransaction();

        // Validate: Bill phải ở trạng thái DEPOSIT_PENDING
        if (bill.getStatus() != StatusBill.DEPOSIT_PENDING) {
            log.warn("Bill {} is not in DEPOSIT_PENDING status. Current status: {}", 
                    bill.getId(), bill.getStatus());
            return buildFailureResult(bill, transaction, verifyResult.getOrderId(), 
                    verifyResult.getResponseCode(), 
                    "Bill is not in DEPOSIT_PENDING status. Current status: " + bill.getStatus());
        }

        // Thanh toán cọc thành công -> chuyển sang REMAINING_PAYMENT_PENDING
        bill.setStatus(StatusBill.REMAINING_PAYMENT_PENDING);
        transaction.setStatus(StatusTransaction.SUCCESS);

        billRepository.save(bill);
        transactionRepository.save(transaction);

        // Gửi email mã code bill cho customer
        sendDepositPaymentSuccessEmail(bill);

        log.info("Deposit payment (30%) processed successfully for bill {}: orderId {}", 
                bill.getId(), verifyResult.getOrderId());

        return buildSuccessResult(bill, transaction, verifyResult.getOrderId(), 
                "Deposit payment (30%) verified and updated successfully");
    }

    /**
     * Xử lý verify cho thanh toán 70% còn lại (REMAINING_PAYMENT_PENDING)
     * 
     * @param verifyResult Kết quả verify từ VNPayVerifySupport
     * @return Map chứa kết quả xử lý
     */
    @Transactional
    public Map<String, Object> handleRemainingPayment(VNPayVerifyResult verifyResult) {
        if (!verifyResult.isVerifySuccess()) {
            // Nếu verify không thành công, xử lý failure
            return handleRemainingPaymentFailure(verifyResult);
        }

        if (!verifyResult.isPaymentSuccess()) {
            // Nếu payment thất bại, xử lý failure
            return handleRemainingPaymentFailure(verifyResult);
        }

        Bill bill = verifyResult.getBill();
        Transaction transaction = verifyResult.getTransaction();

        // Validate: Bill phải ở trạng thái REMAINING_PAYMENT_PENDING
        if (bill.getStatus() != StatusBill.REMAINING_PAYMENT_PENDING) {
            log.warn("Bill {} is not in REMAINING_PAYMENT_PENDING status. Current status: {}", 
                    bill.getId(), bill.getStatus());
            return buildFailureResult(bill, transaction, verifyResult.getOrderId(), 
                    verifyResult.getResponseCode(), 
                    "Bill is not in REMAINING_PAYMENT_PENDING status. Current status: " + bill.getStatus());
        }

        // Thanh toán phần còn lại thành công -> chuyển sang COMPLAINT_PENDING
        bill.setStatus(StatusBill.COMPLAINT_PENDING);
        transaction.setStatus(StatusTransaction.SUCCESS);

        billRepository.save(bill);
        transactionRepository.save(transaction);

        log.info("Remaining payment (70%) processed successfully for bill {}: orderId {}", 
                bill.getId(), verifyResult.getOrderId());

        return buildSuccessResult(bill, transaction, verifyResult.getOrderId(), 
                "Remaining payment (70%) verified and updated successfully");
    }

    /**
     * Xử lý khi deposit payment thất bại
     */
    private Map<String, Object> handleDepositPaymentFailure(VNPayVerifyResult verifyResult) {
        Bill bill = verifyResult.getBill();
        Transaction transaction = verifyResult.getTransaction();

        if (bill == null || transaction == null) {
            log.warn("Cannot handle deposit payment failure: bill or transaction is null");
            return buildFailureResult(bill, transaction, verifyResult.getOrderId(), 
                    verifyResult.getResponseCode(), verifyResult.getErrorMessage());
        }

        // Chỉ xử lý nếu bill đang ở trạng thái DEPOSIT_PENDING
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

            // Update bill và transaction status
            bill.setStatus(StatusBill.PAYMENT_FAILED);
            transaction.setStatus(StatusTransaction.FAILED);

            billRepository.save(bill);
            transactionRepository.save(transaction);

            log.info("Deposit payment failed for bill {}: {}. Daily prices unlocked.", 
                    bill.getId(), verifyResult.getErrorMessage());
        }

        return buildFailureResult(bill, transaction, verifyResult.getOrderId(), 
                verifyResult.getResponseCode(), verifyResult.getErrorMessage());
    }

    /**
     * Xử lý khi remaining payment thất bại
     */

    private Map<String, Object> handleRemainingPaymentFailure(VNPayVerifyResult verifyResult) {
        Bill bill = verifyResult.getBill();
        Transaction transaction = verifyResult.getTransaction();

        if (bill == null || transaction == null) {
            log.warn("Cannot handle remaining payment failure: bill or transaction is null");
            return buildFailureResult(bill, transaction, verifyResult.getOrderId(), 
                    verifyResult.getResponseCode(), verifyResult.getErrorMessage());
        }

        // Chỉ xử lý nếu bill đang ở trạng thái REMAINING_PAYMENT_PENDING
        // KHÔNG unlock homestay vì đã check-in rồi
        if (bill.getStatus() == StatusBill.REMAINING_PAYMENT_PENDING) {
            // Update bill và transaction status
            bill.setStatus(StatusBill.PAYMENT_FAILED);
            transaction.setStatus(StatusTransaction.FAILED);

            billRepository.save(bill);
            transactionRepository.save(transaction);

            log.info("Remaining payment failed for bill {}: {}.", 
                    bill.getId(), verifyResult.getErrorMessage());
        }

        return buildFailureResult(bill, transaction, verifyResult.getOrderId(), 
                verifyResult.getResponseCode(), verifyResult.getErrorMessage());
    }

    /**
     * Gửi email thông báo deposit payment thành công
     */
    private void sendDepositPaymentSuccessEmail(Bill bill) {
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

                try {
                    emailService.sendSimpleEmail(email, content);
                } catch (Exception e) {
                    log.error("Error sending deposit payment success email to {}", email, e);
                }
            }
        }
    }

    /**
     * Build success result
     */
    private Map<String, Object> buildSuccessResult(
            Bill bill, 
            Transaction transaction, 
            String orderId,
            String message
    ) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("billId", bill.getId());
        result.put("billCode", bill.getCode());
        result.put("transactionId", transaction.getId());
        result.put("orderId", orderId);
        result.put("message", message);
        return result;
    }

    /**
     * Build failure result
     */
    private Map<String, Object> buildFailureResult(
            Bill bill, 
            Transaction transaction, 
            String orderId, 
            String responseCode,
            String reason
    ) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        if (bill != null) {
            result.put("billId", bill.getId());
            result.put("billCode", bill.getCode());
        }
        if (transaction != null) {
            result.put("transactionId", transaction.getId());
        }
        result.put("orderId", orderId);
        if (responseCode != null) {
            result.put("responseCode", responseCode);
        }
        result.put("message", reason != null ? reason : "Payment failed");
        return result;
    }
}

