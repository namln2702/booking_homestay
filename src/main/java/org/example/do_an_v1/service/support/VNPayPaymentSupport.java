package org.example.do_an_v1.service.support;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.do_an_v1.configuration.VNPayConfig;
import org.example.do_an_v1.dto.response.VNPayPaymentResponse;
import org.example.do_an_v1.entity.Bill;
import org.example.do_an_v1.entity.HomestayDailyPrice;
import org.example.do_an_v1.entity.Transaction;
import org.example.do_an_v1.enums.StatusBill;
import org.example.do_an_v1.enums.StatusTransaction;
import org.example.do_an_v1.repository.BillRepository;
import org.example.do_an_v1.repository.HomestayDailyPricesRepository;
import org.example.do_an_v1.repository.TransactionRepository;
import org.example.do_an_v1.utils.GenNumber;
import org.example.do_an_v1.utils.VNPayUtil;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * Component hỗ trợ xử lý thanh toán VNPay
 * Có thể tái sử dụng cho nhiều loại transaction (deposit, remaining payment, v.v.)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VNPayPaymentSupport {

    private final VNPayConfig vnPayConfig;
    private final BillRepository billRepository;
    private final TransactionRepository transactionRepository;
    private final HomestayDailyPricesRepository homestayDailyPricesRepository;

    /**
     * Tạo VNPay payment URL cho một transaction
     * 
     * @param transaction Transaction cần thanh toán (phải ở trạng thái PENDING)
     * @param httpRequest HTTP request để lấy IP address
     * @return VNPayPaymentResponse chứa payment URL và thông tin liên quan
     * @throws IllegalArgumentException nếu transaction không hợp lệ
     * @throws IllegalStateException nếu bill không ở trạng thái phù hợp
     */
    @Transactional
    public VNPayPaymentResponse createPaymentUrlForTransaction(
            Transaction transaction,
            HttpServletRequest httpRequest
    ) {
        // Validate transaction
        if (transaction == null) {
            log.error("Transaction cannot be null");
            return null;
        }

        if (transaction.getStatus() != StatusTransaction.PENDING) {
            log.error("Transaction must be in PENDING status. Current status: {}", transaction.getStatus());
            return null;
        }

        // Validate bill
        Bill bill = transaction.getBill();
        if (bill == null) {
            log.error("Transaction must have an associated bill");
            return null;
        }

        // Validate bill status - chỉ cho phép thanh toán khi bill ở các trạng thái phù hợp
        if (bill.getStatus() != StatusBill.DEPOSIT_PENDING 
                && bill.getStatus() != StatusBill.REMAINING_PAYMENT_PENDING) {
            log.error("Bill must be in DEPOSIT_PENDING or REMAINING_PAYMENT_PENDING status. Current status: {}", bill.getStatus());
            return null;
        }

        // Validate: Kiểm tra các ngày đã đặt có trong quá khứ không
        LocalDate checkInLocalDate = bill.getCheckIn().toLocalDate();
        LocalDate today = LocalDate.now();
        
        if (checkInLocalDate.isBefore(today)) {
            log.error("Cannot create payment URL: Bill {} has check-in date {} in the past (today: {})", 
                    bill.getId(), checkInLocalDate, today);
            return null;
        }

        // Validate: Kiểm tra các ngày đã đặt có đang trống (available) không
        // Chỉ kiểm tra cho thanh toán lần đầu (DEPOSIT_PENDING)
        if (bill.getStatus() == StatusBill.DEPOSIT_PENDING) {
            LocalDate checkOutLocalDate = bill.getCheckOut().toLocalDate();
            Date startDate = Date.from(checkInLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date endDate = Date.from(checkOutLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

            // Tìm tất cả HomestayDailyPrice trong khoảng thời gian của bill
            List<HomestayDailyPrice> dailyPrices = homestayDailyPricesRepository.findByHomestayAndDateRange(
                    bill.getHomestay().getId(),
                    startDate,
                    endDate
            );

            // Kiểm tra xem có ngày nào đã được booked bởi bill khác không
            // Reload bill để có collection đầy đủ
            bill = billRepository.findById(bill.getId()).orElse(bill);
            
            // Khởi tạo collection nếu chưa có
            if (bill.getListHomestayDailyPrices() == null) {
                bill.setListHomestayDailyPrices(new java.util.HashSet<>());
            }
            
            for (HomestayDailyPrice dailyPrice : dailyPrices) {
                if (Boolean.TRUE.equals(dailyPrice.getIsBooked())) {

                    
                    // Nếu không thuộc về bill hiện tại và đã được booked, thì đã bị booked bởi bill khác
                    if (dailyPrice.getIsBooked()) {
                        log.error("Cannot create payment URL: Some dates in bill {} are already booked by another bill. Date: {}", 
                                bill.getId(), dailyPrice.getPricePerDay() != null ? dailyPrice.getPricePerDay().getDay() : "unknown");
                        return null;
                    }
                }
            }
        }

        // Khóa các ngày HomestayDailyPrice khi tạo payment URL (chỉ cho lần đầu thanh toán - DEPOSIT_PENDING)
        if (bill.getStatus() == StatusBill.DEPOSIT_PENDING) {
            lockHomestayDailyPrices(bill);
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
        String paymentUrl;
        try {
            paymentUrl = VNPayUtil.createPaymentUrl(
                    vnPayConfig,
                    orderId,
                    amount,
                    orderInfo,
                    ipAddress
            );
        } catch (Exception e) {
            log.error("Error creating VNPay payment URL for transaction {}", transaction.getId(), e);
            return null;
        }

        log.info("Created VNPay payment URL for transaction {} (bill {}): orderId {}", 
                transaction.getId(), bill.getId(), orderId);

        return VNPayPaymentResponse.builder()
                .paymentUrl(paymentUrl)
                .orderId(orderId)
                .amount(amount)
                .message("Payment URL created successfully")
                .build();
    }

    /**
     * Tạo VNPay payment URL cho bill (tự động tìm transaction PENDING)
     * 
     * @param billId ID của bill
     * @param httpRequest HTTP request để lấy IP address
     * @return VNPayPaymentResponse chứa payment URL và thông tin liên quan
     * @throws IllegalArgumentException nếu bill không tồn tại hoặc không có transaction PENDING
     */
    @Transactional
    public VNPayPaymentResponse createPaymentUrlForBill(
            Long billId,
            HttpServletRequest httpRequest
    ) {
        // Tìm bill
        Bill bill = billRepository.findById(billId).orElse(null);
        if (bill == null) {
            log.error("Bill not found with id: {}", billId);
            return null;
        }

        // Tìm transaction PENDING (có thể là transaction cọc hoặc transaction phần còn lại)
        Transaction transaction = transactionRepository.findByBillId(bill.getId()).stream()
                .filter(t -> t.getStatus() == StatusTransaction.PENDING)
                .findFirst()
                .orElse(null);
        
        if (transaction == null) {
            log.error("No pending transaction found for bill id: {}", billId);
            return null;
        }

        return createPaymentUrlForTransaction(transaction, httpRequest);
    }

    /**
     * Tạo VNPay payment URL cho transaction cụ thể bằng transaction ID
     * 
     * @param transactionId ID của transaction
     * @param httpRequest HTTP request để lấy IP address
     * @return VNPayPaymentResponse chứa payment URL và thông tin liên quan
     * @throws IllegalArgumentException nếu transaction không tồn tại
     */
    @Transactional
    public VNPayPaymentResponse createPaymentUrlForTransactionId(
            Long transactionId,
            HttpServletRequest httpRequest
    ) {
        Transaction transaction = transactionRepository.findById(transactionId).orElse(null);
        if (transaction == null) {
            log.error("Transaction not found with id: {}", transactionId);
            return null;
        }

        return createPaymentUrlForTransaction(transaction, httpRequest);
    }

    /**
     * Khóa các ngày HomestayDailyPrice cho bill (set isBooked = true và gán bill)
     * CHỈ áp dụng cho thanh toán lần đầu (30% - DEPOSIT_PENDING)
     * KHÔNG áp dụng cho thanh toán lần 2 (70% - REMAINING_PAYMENT_PENDING)
     */
    @Transactional
    protected void lockHomestayDailyPrices(Bill bill) {
        if (bill == null || bill.getHomestay() == null) {
            log.warn("Cannot lock daily prices: bill or homestay is null");
            return;
        }

        // Đảm bảo chỉ khóa cho thanh toán lần đầu (30%)
        if (bill.getStatus() != StatusBill.DEPOSIT_PENDING) {
            log.warn("Cannot lock daily prices: bill must be in DEPOSIT_PENDING status. Current status: {}", bill.getStatus());
            return;
        }

        // Chuyển đổi LocalDateTime sang Date để query
        LocalDate checkInLocalDate = bill.getCheckIn().toLocalDate();
        LocalDate checkOutLocalDate = bill.getCheckOut().toLocalDate();
        Date startDate = Date.from(checkInLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(checkOutLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        // Tìm tất cả HomestayDailyPrice trong khoảng thời gian của bill
        List<HomestayDailyPrice> dailyPrices = homestayDailyPricesRepository.findByHomestayAndDateRange(
                bill.getHomestay().getId(),
                startDate,
                endDate
        );

        // Khởi tạo collection nếu chưa có
        if (bill.getListHomestayDailyPrices() == null) {
            bill.setListHomestayDailyPrices(new java.util.HashSet<>());
        }

        int lockedCount = 0;
        for (HomestayDailyPrice dailyPrice : dailyPrices) {
            // Chỉ khóa nếu chưa được book
            if (Boolean.FALSE.equals(dailyPrice.getIsBooked()) || dailyPrice.getIsBooked() == null) {
                dailyPrice.setIsBooked(true);
                // Thêm vào collection ManyToMany
                bill.getListHomestayDailyPrices().add(dailyPrice);
                homestayDailyPricesRepository.save(dailyPrice);
                lockedCount++;
            }
        }
        
        // Lưu bill để cập nhật quan hệ ManyToMany
        billRepository.save(bill);

        log.info("Locked {} daily prices for bill {} (homestay {}, checkIn: {}, checkOut: {})",
                lockedCount, bill.getId(), bill.getHomestay().getId(), checkInLocalDate, checkOutLocalDate);
    }
}

