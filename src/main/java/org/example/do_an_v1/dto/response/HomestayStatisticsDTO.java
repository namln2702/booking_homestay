package org.example.do_an_v1.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomestayStatisticsDTO {
    private Long homestayId;
    private String homestayTitle;
    private Long totalBookings; // Tổng số bill đã được đặt
    private BigDecimal totalRevenue; // Tổng số tiền đã kiếm được (từ PAYLOAD_HOST transactions)
    private Long totalComplaints; // Tổng số khiếu nại nhận được
}

