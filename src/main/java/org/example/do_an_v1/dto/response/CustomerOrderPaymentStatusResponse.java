package org.example.do_an_v1.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerOrderPaymentStatusResponse {
    private String phase;
    private boolean awaitingDeposit;
    private boolean depositPaid;
    private LocalDateTime depositPaidAt;
    private BigDecimal depositAmount;

    private boolean awaitingRemainingPayment;
    private boolean remainingPaymentRequired;
    private boolean remainingPaid;
    private LocalDateTime remainingPaidAt;
    private BigDecimal remainingAmount;

    private boolean paymentFailed;
    private boolean awaitingRefund;
    private boolean refunded;
    private LocalDateTime refundCompletedAt;
}
