package org.example.do_an_v1.service.scheduled;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.do_an_v1.entity.Admin;
import org.example.do_an_v1.entity.Bill;
import org.example.do_an_v1.entity.Transaction;
import org.example.do_an_v1.entity.User;
import org.example.do_an_v1.enums.Status;
import org.example.do_an_v1.enums.StatusBill;
import org.example.do_an_v1.enums.StatusTransaction;
import org.example.do_an_v1.enums.TypeTransaction;
import org.example.do_an_v1.repository.AdminRepository;
import org.example.do_an_v1.repository.BillRepository;
import org.example.do_an_v1.repository.TransactionRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

/**
 * Scheduled service để tự động tạo transaction trả tiền cho host
 * Chạy vào 12:00 ngày 25 hàng tháng
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HostPayoutScheduler {

    private final BillRepository billRepository;
    private final TransactionRepository transactionRepository;
    private final AdminRepository adminRepository;

    /**
     * Chạy vào 12:00 ngày 25 hàng tháng
     * Tổng hợp các bills có status SUCCEED, REJECTED, CANCELLED
     * từ ngày 25 tháng trước đến ngày 25 tháng này
     * và tạo transaction trả tiền cho host nếu chưa có
     */
    @Scheduled(cron = "0 0 12 25 * ?") // Chạy vào 12:00 ngày 25 hàng tháng
    @Transactional
    public void processMonthlyHostPayouts() {
        log.info("Starting scheduled task: Process monthly host payouts");

        // Lấy admin user để tạo transaction
        User adminUser = adminRepository.findAll().stream()
                .filter(admin -> admin.getStatus() == Status.ACTIVE)
                .map(Admin::getUser)
                .findFirst()
                .orElse(null);

        if (adminUser == null) {
            log.error("No active admin found to process host payouts");
            return;
        }

        // Tính toán khoảng thời gian: từ 25 tháng trước đến 25 tháng này
        LocalDate today = LocalDate.now();
        YearMonth currentMonth = YearMonth.from(today);
        YearMonth previousMonth = currentMonth.minusMonths(1);

        // Ngày 25 tháng trước (bắt đầu từ 00:00:00)
        LocalDate startDate = previousMonth.atDay(Math.min(25, previousMonth.lengthOfMonth()));
        LocalDateTime startDateTime = startDate.atStartOfDay();

        // Ngày 25 tháng này (kết thúc vào 23:59:59.999)
        LocalDate endDate = currentMonth.atDay(Math.min(25, currentMonth.lengthOfMonth()));
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59, 999_999_999);

        log.info("Processing bills from {} to {}", startDateTime, endDateTime);

        // Lấy bills có status SUCCEED, REJECTED, CANCELLED trong khoảng thời gian
        List<Bill> succeedBills = billRepository.findByStatusAndCreatedAtBetween(
                StatusBill.SUCCEED, startDateTime, endDateTime);
        List<Bill> rejectedBills = billRepository.findByStatusAndCreatedAtBetween(
                StatusBill.REJECTED, startDateTime, endDateTime);
        List<Bill> cancelledBills = billRepository.findByStatusAndCreatedAtBetween(
                StatusBill.CANCELLED, startDateTime, endDateTime);

        int totalProcessed = 0;
        int totalCreated = 0;
        BigDecimal totalAmount = BigDecimal.ZERO;

        // Xử lý bills SUCCEED
        for (Bill bill : succeedBills) {
            if (processBillPayout(bill, adminUser)) {
                totalCreated++;
                BigDecimal totalReceived = calculateTotalReceivedAmount(bill);
                BigDecimal commission = bill.getCommission() != null ? bill.getCommission() : BigDecimal.ZERO;
                BigDecimal payoutAmount = totalReceived.subtract(commission);
                totalAmount = totalAmount.add(payoutAmount);
            }
            totalProcessed++;
        }

        // Xử lý bills REJECTED
        for (Bill bill : rejectedBills) {
            if (processBillPayout(bill, adminUser)) {
                totalCreated++;
                BigDecimal totalReceived = calculateTotalReceivedAmount(bill);
                BigDecimal commission = bill.getCommission() != null ? bill.getCommission() : BigDecimal.ZERO;
                BigDecimal payoutAmount = totalReceived.subtract(commission);
                totalAmount = totalAmount.add(payoutAmount);
            }
            totalProcessed++;
        }

        // Xử lý bills CANCELLED
        for (Bill bill : cancelledBills) {
            if (processBillPayout(bill, adminUser)) {
                totalCreated++;
                BigDecimal totalReceived = calculateTotalReceivedAmount(bill);
                BigDecimal commission = bill.getCommission() != null ? bill.getCommission() : BigDecimal.ZERO;
                BigDecimal payoutAmount = totalReceived.subtract(commission);
                totalAmount = totalAmount.add(payoutAmount);
            }
            totalProcessed++;
        }

        log.info("Completed scheduled task: Process monthly host payouts. " +
                "Total bills processed: {}, New transactions created: {}, Total amount: {}",
                totalProcessed, totalCreated, totalAmount);
    }

    /**
     * Xử lý payout cho một bill
     * @param bill Bill cần xử lý
     * @param adminUser Admin user để tạo transaction
     * @return true nếu tạo transaction mới, false nếu đã có transaction rồi
     */
    private boolean processBillPayout(Bill bill, User adminUser) {
        // Kiểm tra xem đã có transaction ADMIN_PAYMENT_HOST cho bill này chưa
        List<Transaction> billTransactions = transactionRepository.findByBill(bill);
        boolean hasPayout = billTransactions.stream()
                .anyMatch(t -> t.getTransactionType() == TypeTransaction.ADMIN_PAYMENT_HOST);

        if (hasPayout) {
            log.debug("Bill {} already has ADMIN_PAYMENT_HOST transaction, skipping", bill.getId());
            return false;
        }

        // Tính tổng tiền đã nhận từ customer
        BigDecimal totalReceived = calculateTotalReceivedAmount(bill);

        // Nếu không có tiền nào được nhận, không tạo transaction
        if (totalReceived.compareTo(BigDecimal.ZERO) <= 0) {
            log.debug("Bill {} has no received amount, skipping payout", bill.getId());
            return false;
        }

        // Kiểm tra bill có host và user của host không
        if (bill.getHomestay() == null || bill.getHomestay().getHost() == null
                || bill.getHomestay().getHost().getUser() == null) {
            log.warn("Bill {} has no host user, skipping payout", bill.getId());
            return false;
        }

        // Trừ commission (hoa hồng của admin) khỏi số tiền trả cho host
        BigDecimal commission = bill.getCommission();
        if (commission == null) {
            commission = BigDecimal.ZERO;
        }
        BigDecimal payoutAmount = totalReceived.subtract(commission);

        // Nếu số tiền trả cho host <= 0 sau khi trừ commission, không tạo transaction
        if (payoutAmount.compareTo(BigDecimal.ZERO) <= 0) {
            log.debug("Bill {} payout amount after commission deduction is zero or negative, skipping payout", bill.getId());
            return false;
        }

        User hostUser = bill.getHomestay().getHost().getUser();

        // Tạo transaction ADMIN_PAYMENT_HOST
        Transaction payoutTransaction = Transaction.builder()
                .amount(payoutAmount)
                .transactionType(TypeTransaction.ADMIN_PAYMENT_HOST)
                .status(StatusTransaction.PENDING) // Chờ admin xác nhận
                .fromUser(adminUser)
                .toUser(hostUser)
                .bill(bill)
                .build();

        transactionRepository.save(payoutTransaction);
        log.info("Created ADMIN_PAYMENT_HOST transaction for bill {}. Total received: {}, Commission: {}, Payout amount: {}, Host: {}",
                bill.getId(), totalReceived, commission, payoutAmount, hostUser.getId());

        return true;
    }

    /**
     * Tính tổng tiền đã nhận từ customer cho bill này
     * Bao gồm CUSTOMER_PAYMENT_ADMIN_FIRST và CUSTOMER_PAYMENT_ADMIN_SECOND thành công
     */
    private BigDecimal calculateTotalReceivedAmount(Bill bill) {
        List<Transaction> billTransactions = transactionRepository.findByBill(bill);

        return billTransactions.stream()
                .filter(t -> (t.getTransactionType() == TypeTransaction.CUSTOMER_PAYMENT_ADMIN_FIRST
                        || t.getTransactionType() == TypeTransaction.CUSTOMER_PAYMENT_ADMIN_SECOND
                        || t.getTransactionType() == TypeTransaction.CUSTOMER_PAYMENT_ADMIN)
                        && t.getStatus() == StatusTransaction.SUCCESS)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

