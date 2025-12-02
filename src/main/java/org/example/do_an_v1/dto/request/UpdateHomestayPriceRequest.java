package org.example.do_an_v1.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateHomestayPriceRequest {
    @NotNull(message = "Homestay ID is required")
    private Long homestayId;

    @NotNull(message = "Daily prices list is required")
    private List<DailyPriceUpdate> dailyPrices;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DailyPriceUpdate {
        @NotNull(message = "Day is required")
        private Date day;
        
        @NotNull(message = "Price is required")
        private Float price;
    }
}

