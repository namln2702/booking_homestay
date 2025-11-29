package org.example.do_an_v1.enums;

public enum StatusBill {

    /**
     * Khách hàng đã đặt nhưng chưa thanh toán.
     * Đơn sẽ bị hủy nếu quá hạn thanh toán.
     */
    PAYMENT_PENDING(1),

    /**
     * Thanh toán thất bại hoặc khách không thanh toán đúng hạn.
     * Đơn bị hủy, không có giao dịch tiền.
     */
    PAYMENT_FAILED(2),

    /**
     * Khách hàng thanh toán thành công.
     * Tiền được chuyển từ customer → admin (escrow).
     * Đơn đang chờ đến ngày check-in.
     */
    CHECKIN_PENDING(3),

    /**
     * Khách không check-in đúng hạn.
     * Đơn hoàn tất và admin chuyển tiền cho host.
     */
    CHECKIN_EXPIRED(4),

    /**
     * Khách đã check-in thành công và hoàn tất lưu trú.
     * Đơn chuyển sang giai đoạn chờ khiếu nại (7 ngày).
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
     * Khiếu nại được chuyển cho Admin xử lý
     * (do Host từ chối hoặc quá hạn).
     * Admin có thể:
     *  - Đồng ý hoặc quá hạn xử lý → REFUNDED
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
     * Đơn kết thúc mà không có hoàn tiền.
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