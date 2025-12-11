package org.example.do_an_v1.enums;

public enum StatusBill {

    /**
     * Khách hàng đã đặt nhưng chưa thanh toán cọc.
     * Đơn sẽ bị hủy nếu quá hạn thanh toán.
     */
    DEPOSIT_PENDING(0),

    /**
     * Khách đã thanh toán cọc thành công.
     * Đơn đang chờ đến ngày check-in.
     */
    DEPOSIT_PAID(11),

    /**
     * Khi khách đến homestay và cần nhập mã check-in (checkinCode).
     */
    CHECKIN_VERIFY(12),

    /**
     * Khách cần thanh toán nốt phần còn lại của đơn hàng.
     */
    REMAINING_PAYMENT_PENDING(13),

    /**
     * Thanh toán thất bại hoặc khách không thanh toán đúng hạn.
     * Đơn bị hủy, không có giao dịch tiền.
     */
    PAYMENT_FAILED(2),

    /**
     * Khách hàng đã thanh toán đầy đủ (cọc + phần còn lại).
     * Đơn đang chờ đến ngày check-in hoặc hoàn tất check-in.
     */
    CHECKIN_PENDING(3),

    /**
     * Khách không check-in đúng hạn.
     * Đơn hoàn tất và admin chuyển tiền cho host.
     */
    CHECKIN_EXPIRED(4),

    /**
     * Khách đã check-out.
     * Đơn bước vào giai đoạn chờ khiếu nại trong (N + 1) ngày,
     * với N là số ngày lưu trú thực tế của khách hàng.
     */
    COMPLAINT_PENDING(5),

    /**
     * Khiếu nại đang được xử lý bởi Host (2 ngày).
     * Host có thể:
     *  - Đồng ý → chuyển sang REFUNDED
     *  - Từ chối hoặc quá hạn → chuyển sang ADMIN_COMPLAINT_PROCESSING
     */
    HOST_COMPLAINT_PROCESSING(6),

    /**
     * Khiếu nại được chuyển cho Admin xử lý (1 ngày).
     * Admin có thể:
     *  - Đồng ý → REFUNDED
     *  - Không đồng ý → REJECTED
     */
    ADMIN_COMPLAINT_PROCESSING(7),

    /**
     * Khiếu nại được xử lý thành công.
     * Admin hoàn tiền cho Customer (admin → customer).
     */
    REFUNDED(8),

    /**
     * Khiếu nại bị từ chối.
     * Đơn kết thúc mà không hoàn tiền.
     */
    REJECTED(9),

    /**
     * Đơn hoàn tất thành công.
     * Không còn khiếu nại, admin chuyển tiền cho host.
     */
    SUCCEED(10);

    private final int code;

    StatusBill(int code){
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}