package org.example.do_an_v1.service.scheduled;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.do_an_v1.entity.Bill;
import org.example.do_an_v1.enums.StatusBill;
import org.example.do_an_v1.repository.BillRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled service để tự động chuyển trạng thái bill khi complaint processing quá hạn
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ComplaintProcessingScheduler {

    private final BillRepository billRepository;

    /**
     * Task 1: Check hàng ngày lúc 00:00
     * Nếu bill ở trạng thái HOST_COMPLAINT_PROCESSING và updatedAt > 2 ngày
     * thì chuyển sang ADMIN_COMPLAINT_PROCESSING
     */
    @Scheduled(cron = "0 0 0 * * ?") // Chạy mỗi ngày lúc 00:00:00
    @Transactional
    public void checkHostComplaintProcessingExpiration() {
        log.info("Starting scheduled task: Check HOST_COMPLAINT_PROCESSING bills expiration");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime twoDaysAgo = now.minusDays(2);

        // Lấy tất cả bills ở trạng thái HOST_COMPLAINT_PROCESSING
        List<Bill> hostProcessingBills = billRepository.findByStatus(StatusBill.HOST_COMPLAINT_PROCESSING);

        int updatedCount = 0;
        for (Bill bill : hostProcessingBills) {
            // Kiểm tra nếu updatedAt > 2 ngày so với hiện tại
            if (bill.getUpdatedAt() != null && bill.getUpdatedAt().isBefore(twoDaysAgo)) {
                bill.setStatus(StatusBill.ADMIN_COMPLAINT_PROCESSING);
                billRepository.save(bill);
                updatedCount++;
                log.info("Bill ID {} status changed from HOST_COMPLAINT_PROCESSING to ADMIN_COMPLAINT_PROCESSING. " +
                        "Last updated: {}", bill.getId(), bill.getUpdatedAt());
            }
        }

        log.info("Completed scheduled task: Check HOST_COMPLAINT_PROCESSING bills expiration. " +
                "Total bills checked: {}, Bills updated: {}", hostProcessingBills.size(), updatedCount);
    }

    /**
     * Task 2: Check hàng ngày lúc 00:00
     * Nếu bill ở trạng thái ADMIN_COMPLAINT_PROCESSING và updatedAt > 1 ngày
     * thì chuyển sang REFUNDED_PENDING
     */
    @Scheduled(cron = "0 0 0 * * ?") // Chạy mỗi ngày lúc 00:00:00
    @Transactional
    public void checkAdminComplaintProcessingExpiration() {
        log.info("Starting scheduled task: Check ADMIN_COMPLAINT_PROCESSING bills expiration");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneDayAgo = now.minusDays(1);

        // Lấy tất cả bills ở trạng thái ADMIN_COMPLAINT_PROCESSING
        List<Bill> adminProcessingBills = billRepository.findByStatus(StatusBill.ADMIN_COMPLAINT_PROCESSING);

        int updatedCount = 0;
        for (Bill bill : adminProcessingBills) {
            // Kiểm tra nếu updatedAt > 1 ngày so với hiện tại
            if (bill.getUpdatedAt() != null && bill.getUpdatedAt().isBefore(oneDayAgo)) {
                bill.setStatus(StatusBill.REFUNDED_PENDING);
                billRepository.save(bill);
                updatedCount++;
                log.info("Bill ID {} status changed from ADMIN_COMPLAINT_PROCESSING to REFUNDED_PENDING. " +
                        "Last updated: {}", bill.getId(), bill.getUpdatedAt());
            }
        }

        log.info("Completed scheduled task: Check ADMIN_COMPLAINT_PROCESSING bills expiration. " +
                "Total bills checked: {}, Bills updated: {}", adminProcessingBills.size(), updatedCount);
    }
}

