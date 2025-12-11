package org.example.do_an_v1.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.example.do_an_v1.enums.StatusBill;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO cho Bill (Hóa đơn đặt homestay)
 * 
 * Khi booking: 
 * - id: ID của homestay (dùng tạm thời, sẽ được map đúng trong service)
 * - checkIn, checkOut: Bắt buộc
 * - homestayDailyPricesDTOS: Danh sách giá theo ngày
 * - customerBookingInfoDTO: Thông tin người đặt (nếu là người mới, optional)
 * 
 * Response:
 * - Đầy đủ thông tin Bill sau khi tạo
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillDTO {

    private Long id;                      // ID hóa đơn (khi response) hoặc ID homestay (khi request booking)
    private String code;                  // Mã đơn (unique code)
    private StatusBill status;            // Trạng thái hóa đơn (DEPOSIT_PENDING, CHECKIN_PENDING, SUCCEED, ...)
    private LocalDateTime createdAt;      // Thời gian tạo đơn
    private LocalDateTime updatedAt;      // Cập nhật gần nhất
    
    @NotNull(message = "Check-in date is required")
    private LocalDateTime checkIn;        // Ngày check-in
    
    @NotNull(message = "Check-out date is required")
    private LocalDateTime checkOut;       // Ngày check-out
    
    private LocalDateTime actualCheckin;  // Thời gian check-in thực tế (nếu có)

    // --- Thông tin giá theo ngày ---
    @Valid
    private List<HomestayDailyPricesDTO> homestayDailyPricesDTOS;

    // --- Thông tin khách hàng (Customer profile) ---
    private CustomerDTO customerDTO;

    // --- Thông tin homestay ---
    private HomestaySummaryDTO homestayDTO;

    // --- Thông tin địa chỉ homestay ---
    private AddressDTO addressDTO;

    // --- Thông tin người đặt (Booking contact info - nếu khác với customer) ---
    @Valid
    private CustomerBookingInfoDTO customerBookingInfoDTO;

    // --- Thông tin thanh toán ---
    private List<TransactionDTO> transactions;  // Danh sách giao dịch liên quan
}