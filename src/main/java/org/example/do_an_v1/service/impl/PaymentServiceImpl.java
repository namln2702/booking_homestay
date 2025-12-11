package org.example.do_an_v1.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.do_an_v1.dto.request.PaymentNotificationRequest;
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
import org.example.do_an_v1.service.PaymentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final BillRepository billRepository;
    private final TransactionRepository transactionRepository;
    private final HomestayDailyPricesRepository homestayDailyPricesRepository;
    private final EmailService emailService;

    @Override
    @Transactional
    public ApiResponse<?> handlePaymentNotification(PaymentNotificationRequest request) {
        if (request == null || request.getBillId() == null) {
            throw new IllegalArgumentException("Bill ID is required");
        }
        if (request.getSuccess() == null) {
            throw new IllegalArgumentException("Success status is required");
        }

        Bill bill = billRepository.findById(request.getBillId())
                .orElseThrow(() -> new RuntimeException("Bill not found with id: " + request.getBillId()));

        // Validate: Bill phải ở trạng thái DEPOSIT_PENDING
        if (bill.getStatus() != StatusBill.DEPOSIT_PENDING) {
            throw new IllegalStateException("Bill must be in DEPOSIT_PENDING status. Current status: " + bill.getStatus());
        }

        // Tìm transaction
        Transaction transaction = transactionRepository.findByBillId(request.getBillId())
                .orElseThrow(() -> new RuntimeException("Transaction not found for bill id: " + request.getBillId()));

        if (request.getSuccess()) {
            // Thanh toán thành công
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

            return new ApiResponse<>(200, "Payment successful. Bill status changed to CHECKIN_PENDING", null);
        } else {
            // Thanh toán thất bại - unlock homestay_daily_prices
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

            return new ApiResponse<>(200, "Payment failed. Bill status changed to PAYMENT_FAILED and daily prices unlocked", null);
        }
    }
}

