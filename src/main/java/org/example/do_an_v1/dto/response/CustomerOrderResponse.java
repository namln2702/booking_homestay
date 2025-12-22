package org.example.do_an_v1.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.do_an_v1.enums.StatusBill;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Lightweight view of a bill for the customer's "My Orders" screen.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerOrderResponse {
    private Long billId;
    private String billCode;
    private StatusBill status;
    private String homestayName;
    private Long homestayId;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private BigDecimal totalAmount;
    private BigDecimal depositAmount;
    private LocalDateTime createdAt;
    private Float basePrice;
    private List<CustomerOrderDailyPriceResponse> dailyPrices;
}
