package org.example.do_an_v1.service.scheduled;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.do_an_v1.entity.Bill;
import org.example.do_an_v1.entity.Complaint;
import org.example.do_an_v1.enums.StatusBill;
import org.example.do_an_v1.repository.BillRepository;
import org.example.do_an_v1.repository.ComplaintRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled task để kiểm tra và cập nhật các bill hết thời gian khiếu nại
 * Chạy mỗi ngày lúc 00:00
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ComplaintExpirationScheduler {

    private final BillRepository billRepository;
    private final ComplaintRepository complaintRepository;

    /**
     * Chạy mỗi ngày lúc 00:00:00
     * Cron expression: "0 0 0 * * ?" = second minute hour day month dayOfWeek
     */
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void checkAndUpdateExpiredComplaintBills() {
        log.info("Starting scheduled task: Check and update expired complaint bills");

        try {
            // Tìm tất cả bills ở trạng thái COMPLAINT_PENDING
            List<Bill> complaintPendingBills = billRepository.findAll().stream()
                    .filter(bill -> bill.getStatus() == StatusBill.COMPLAINT_PENDING)
                    .toList();

            log.info("Found {} bills with COMPLAINT_PENDING status", complaintPendingBills.size());

            int updatedCount = 0;
            LocalDateTime now = LocalDateTime.now();

            for (Bill bill : complaintPendingBills) {
                // Tính deadline: 1 ngày sau checkout
                LocalDateTime checkoutTime = bill.getCheckOut();
                LocalDateTime complaintDeadline = checkoutTime.plusDays(1);

                // Kiểm tra xem đã hết thời gian khiếu nại chưa
                if (now.isAfter(complaintDeadline) || now.isEqual(complaintDeadline)) {
                    // Kiểm tra xem có complaint nào cho bill này không
                    List<Complaint> complaints = complaintRepository.findByBill(bill);
                    
                    if (complaints.isEmpty()) {
                        // Không có complaint -> chuyển thành SUCCEED
                        bill.setStatus(StatusBill.SUCCEED);
                        billRepository.save(bill);
                        updatedCount++;
                        
                        log.info("Updated bill {} from COMPLAINT_PENDING to SUCCEED (no complaint, deadline passed: {})", 
                                bill.getId(), complaintDeadline);
                    } else {
                        // Có complaint -> giữ nguyên status (sẽ được xử lý bởi host/admin)
                        log.debug("Bill {} has {} complaint(s), keeping status COMPLAINT_PENDING", 
                                bill.getId(), complaints.size());
                    }
                }
            }

            log.info("Scheduled task completed. Updated {} bills from COMPLAINT_PENDING to SUCCEED", updatedCount);

        } catch (Exception e) {
            log.error("Error in scheduled task: checkAndUpdateExpiredComplaintBills", e);
        }
    }
}

