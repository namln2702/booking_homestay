package org.example.do_an_v1.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Thống kê doanh thu chi tiết
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueStatisticsResponse {

    /**
     * Số tiền đã trả cho customer (từ transaction REFUND với status SUCCESS)
     */
    private BigDecimal customerRefundPaid;

    /**
     * Số tiền chưa trả cho customer (các bill có status REFUNDED_PENDING hoặc CANCEL_REFUND_PENDING)
     */
    private BigDecimal customerRefundPending;

    /**
     * Số tiền host dự định sẽ nhận được trong tháng
     * (Từ các bill có status SUCCEED, REJECTED, CANCELLED, CHECKIN_EXPIRED
     * và có transaction ADMIN_PAYMENT_HOST với status PENDING)
     */
    private BigDecimal hostExpectedAmount;

    /**
     * Số hoa hồng admin dự định nhận được
     * (Từ commission của các bill dự định sẽ trả cho host)
     */
    private BigDecimal adminExpectedCommission;

    /**
     * Số tiền ADMIN đã nhận được của customer
     * (Từ các transaction CUSTOMER_PAYMENT_ADMIN_* với status SUCCESS)
     */
    private BigDecimal adminReceivedFromCustomer;

    /**
     * Tỷ lệ hoa hồng (commission rate) từ 0-1
     * Ví dụ: 0.1 = 10%, 0.15 = 15%
     */
    private BigDecimal commission;

    /**
     * Số tiền host đã nhận được
     * (Từ các bill có status SUCCEED, REJECTED, CANCELLED, CHECKIN_EXPIRED
     * và có transaction ADMIN_PAYMENT_HOST với status SUCCESS)
     */
    private BigDecimal hostReceivedAmount;

    /**
     * Số tiền host dự kiến nhận được
     * (Từ các bill có status SUCCEED, REJECTED, CANCELLED, CHECKIN_EXPIRED
     * và có transaction ADMIN_PAYMENT_HOST với status PENDING)
     */
    private BigDecimal hostPendingAmount;

    /**
     * ID của host (dùng cho API thống kê của host)
     */
    private Long hostId;
}

