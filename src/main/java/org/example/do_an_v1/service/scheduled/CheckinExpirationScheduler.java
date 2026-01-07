package org.example.do_an_v1.service.scheduled;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.do_an_v1.entity.Bill;
import org.example.do_an_v1.entity.HomestayDailyPrice;
import org.example.do_an_v1.enums.StatusBill;
import org.example.do_an_v1.repository.BillRepository;
import org.example.do_an_v1.repository.HomestayDailyPricesRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled task để kiểm tra và cập nhật các bill hết thời gian check-in
 * Chạy mỗi ngày lúc 23:59:59 để check các bill có checkIn date là ngày hôm nay
 * và status là REMAINING_PAYMENT_PENDING thì chuyển sang CHECKIN_EXPIRED
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CheckinExpirationScheduler {

    private final BillRepository billRepository;
    private final HomestayDailyPricesRepository homestayDailyPricesRepository;

    /**
     * Chạy mỗi ngày lúc 23:59:59
     * Cron expression: "59 59 23 * * ?" = second minute hour day month dayOfWeek
     */
    @Scheduled(cron = "59 59 23 * * ?")
    @Transactional
    public void checkAndExpireCheckinBills() {
        log.info("Starting scheduled task: Check and expire bills with REMAINING_PAYMENT_PENDING status on check-in date");

        try {
            LocalDate today = LocalDate.now();

            // Tìm tất cả bills ở trạng thái REMAINING_PAYMENT_PENDING
            List<Bill> remainingPaymentPendingBills = billRepository.findAll().stream()
                    .filter(bill -> bill.getStatus() == StatusBill.REMAINING_PAYMENT_PENDING)
                    .filter(bill -> {
                        LocalDateTime checkIn = bill.getCheckIn();
                        if (checkIn == null) {
                            return false;
                        }
                        // Kiểm tra xem checkIn date có cùng ngày với hôm nay không
                        LocalDate checkInDate = checkIn.toLocalDate();
                        return checkInDate.equals(today);
                    })
                    .toList();

            log.info("Found {} bills with REMAINING_PAYMENT_PENDING status on check-in date {}", 
                    remainingPaymentPendingBills.size(), today);

            int updatedCount = 0;

            for (Bill bill : remainingPaymentPendingBills) {
                try {
                    // Unlock homestay_daily_prices (giống như cancelBill)
                    // Reload bill để có collection đầy đủ
                    bill = billRepository.findById(bill.getId()).orElse(bill);
                    
                    if (bill.getListHomestayDailyPrices() != null) {
                        for (HomestayDailyPrice dailyPrice : bill.getListHomestayDailyPrices()) {
                            dailyPrice.setIsBooked(false);
                            homestayDailyPricesRepository.save(dailyPrice);
                        }
                        // Xóa tất cả quan hệ ManyToMany
                        bill.getListHomestayDailyPrices().clear();
                    }

                    // Cập nhật status bill thành CHECKIN_EXPIRED
                    bill.setStatus(StatusBill.CHECKIN_EXPIRED);
                    billRepository.save(bill);

                    updatedCount++;
                    log.info("Bill ID {} status changed from REMAINING_PAYMENT_PENDING to CHECKIN_EXPIRED. " +
                            "Check-in date: {}", bill.getId(), bill.getCheckIn());

                } catch (Exception e) {
                    log.error("Error processing bill ID {}: {}", bill.getId(), e.getMessage(), e);
                }
            }

            log.info("Completed scheduled task: Check and expire check-in bills. " +
                    "Total bills checked: {}, Bills updated: {}", remainingPaymentPendingBills.size(), updatedCount);

        } catch (Exception e) {
            log.error("Error in scheduled task checkAndExpireCheckinBills: {}", e.getMessage(), e);
        }
    }
}

