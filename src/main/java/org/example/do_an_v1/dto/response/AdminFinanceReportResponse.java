package org.example.do_an_v1.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Tổng hợp báo cáo tài chính cho admin: thu từ customer, chi cho host, khoản hoàn.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminFinanceReportResponse {

    private long totalBills;
    private long completedBills;
    private BigDecimal totalRevenueFromCustomers;
    private BigDecimal totalPayoutToHosts;
    private BigDecimal totalRefundsToCustomers;
    private BigDecimal netRevenue;
}
