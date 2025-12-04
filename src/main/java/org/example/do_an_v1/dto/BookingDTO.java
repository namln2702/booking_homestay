package org.example.do_an_v1.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * DTO cho Booking (Đặt homestay)
 * 
 * Dùng khi customer tạo booking mới
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingDTO {

    @NotNull(message = "homstayid is required")
    private Long homestayId;        

    @NotNull(message = "Check-in date is required")
    private Date checkIn;        // Ngày check-in

    @NotNull(message = "Check-out date is required")
    private Date checkOut;       // Ngày check-out

    // --- Danh sách ID của HomestayDailyPrice ---
    private List<Long> homestayDailyPriceIds;

    // --- Thông tin người đặt (Booking contact info - nếu khác với customer) ---
    @Valid
    private CustomerBookingInfoDTO customerBookingInfoDTO;
}

