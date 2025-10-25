package org.example.do_an_v1.enums;

public enum StatusBill {


    /**
     * Khách hàng đã đặt nhưng chưa hoàn tất thanh toán.
     * Đơn sẽ bị hủy nếu quá hạn thanh toán.
     */
    PAYMENT_PENDING(1),

    /**
     * Thanh toán thất bại hoặc khách hàng không thanh toán đúng hạn.
     * Đơn bị hủy, không có giao dịch tiền.
     */
    PAYMENT_FAILED(2),

    /**
     * Khách hàng đã thanh toán thành công.
     * Tiền được chuyển từ customer → admin.
     * Đơn đang chờ đến ngày check-in.
     */
    CHECKIN_PENDING(3),

    /**
     * Khách không check-in đúng hạn.
     * Đơn tự động được coi là hoàn tất,
     * tiền được chuyển từ admin → host.
     */
    CHECKIN_EXPIRED(4),

    /**
     * Khách đã check-in thành công.
     * Đơn đang trong thời gian chờ khiếu nại sau khi ở xong.
     */
    COMPLAINT_PENDING(5),

    /**
     * Quá thời hạn khiếu nại, không phát sinh tranh chấp.
     * Đơn được hoàn tất và admin chuyển tiền cho host.
     */
    COMPLAINT_EXPIRED(6),

    /**
     * Khiếu nại đang được xử lý bởi host.
     * Host có thể đồng ý hoặc từ chối yêu cầu khiếu nại của khách.
     */
    HOST_COMPLAINT_PROCESSING(7),

    /**
     * Host đồng ý xử lý khiếu nại.
     * Khiếu nại được chấp nhận, admin hoàn tiền cho khách hàng.
     */
    HOST_COMPLAINT_APPROVED(8),

    /**
     * Admin từ chối xử lý khiếu nại hoặc xác định khiếu nại không hợp lệ.
     * Đơn không được hoàn tiền và coi như hoàn tất bình thường.
     */
    ADMIN_COMPLAINT_REJECTED(9),

    /**
     * Khiếu nại bị từ chối (do admin hoặc do hết thời hạn xử lý).
     * Đơn kết thúc không hoàn tiền cho khách.
     */
    REJECTED(10),

    /**
     * Khiếu nại được xử lý thành công.
     * Admin hoàn tiền cho customer (tiền đi ngược lại).
     */
    REFUNDED(11),

    /**
     * Đơn hàng hoàn tất thành công.
     * Không còn khiếu nại, tiền từ admin → host.
     */
    SUCCEED(12);



    private int code;

    StatusBill(int code){
        this.code = code;
    }
}
