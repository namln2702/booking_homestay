package org.example.do_an_v1.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO cho giá homestay theo ngày
 * 
 * Dùng trong BookingDTO khi booking để chỉ định giá cho từng ngày
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HomestayDailyPricesDTO {
    private Long id;                      // ID của HomestayDailyPrice (khi response)
    
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private Float price;                  // Giá cho ngày đó
}
