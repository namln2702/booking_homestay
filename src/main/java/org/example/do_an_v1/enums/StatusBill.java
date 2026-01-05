package org.example.do_an_v1.enums;

public enum StatusBill {

    /**
     * Khách hàng đã đặt nhưng chưa thanh toán cọc.
     * Đơn sẽ bị hủy nếu quá hạn thanh toán.
     */
    DEPOSIT_PENDING(0),

    /**
     * Thanh toán đợt 1 thất bại
     * Đơn bị hủy, không có giao dịch tiền.
     * Status cuối của luồng thanh toán đợt 1.
     */
    DEPOSIT_FAILED(11),


    /**
     * Khách cần check in và thanh toán nốt phần còn lại của đơn hàng.
     */
    REMAINING_PAYMENT_PENDING(13),

    /**
     * Thanh toán thất bại hoặc khách không thanh toán đúng hạn.
     * Đơn bị hủy, không có giao dịch tiền.
     * Status cuối của luồng thanh toán đợt 2.
     */
    REMAINING_PAYMENT_FAILED(2),


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

    /*
       * Chờ được admin thanh toán
     */
    REFUNDED_PENDING(14),
    /**
     * Khiếu nại được xử lý thành công.
     * Admin hoàn tiền cho Customer (admin → customer).
     * Status cuối của luồng refund
     */
    REFUNDED(8),

    /**
     * Khiếu nại bị từ chối.
     * Đơn kết thúc mà không hoàn tiền.
     * Luồng thanh toán cuối của luồng refund
     */
    REJECTED(9),

    /**
     * Đơn hoàn tất thành công.
     * Không còn khiếu nại, admin chuyển tiền cho host.
     * Luồng thanh toán cuối của luồng thanh toán
     */
    SUCCEED(10),

    /**
     * Đơn bị customer hủy, đang chờ thanh toán hoàn tiền
     * Chuyển sang CANCELLED_REFUNDED khi thanh toán hoàn tiền thành công
     */
    CANCEL_REFUND_PENDING(17),

    /**
     * Đơn bị customer hủy, và được hoàn tiền
     * Luồng thanh toán cuối của luồng thanh toán
     */
    CANCELLED_REFUNDED (16),

    /**
     * Đơn bị customer hủy, và không được hoàn tiền
     * Luồng thanh toán cuối của luồng thanh toán sau khi thanh toán xong đợt 1
     */
    CANCELLED (15);

    private final int code;

    StatusBill(int code){
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}