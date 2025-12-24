package org.example.do_an_v1.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.example.do_an_v1.dto.request.PersonCapacityRequest;
import org.example.do_an_v1.dto.request.PricePerDayRequest;

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

    // --- Danh sách PricePerDay (ngày và giá) ---
    @NotNull(message = "PricePerDays list is required")
    @NotEmpty(message = "PricePerDays list cannot be empty")
    @Valid
    private List<PricePerDayRequest> pricePerDays;

    @NotNull(message = "listPersonHomestay is required")
    @NotEmpty(message = "listPersonHomestay cannot be empty")
    @Valid
    private List<PersonCapacityRequest> listPersonHomestay;

    // --- Thông tin người đặt (Booking contact info - nếu khác với customer) ---
    @Valid
    private CustomerBookingInfoDTO customerBookingInfoDTO;
}
