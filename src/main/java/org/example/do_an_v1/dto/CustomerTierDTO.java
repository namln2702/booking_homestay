package org.example.do_an_v1.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.do_an_v1.enums.CustomerTier;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerTierDTO {
    private Long userId;
    private CustomerTier tier;
    private int successfulBookings;
    private int refundedBookings;
    private double effectiveOrders;
}
