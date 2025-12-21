package org.example.do_an_v1.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Describes the booked price for a specific day within a bill.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerOrderDailyPriceResponse {
    private Long dailyPriceId;
    private LocalDate date;
    private Float price;
}
