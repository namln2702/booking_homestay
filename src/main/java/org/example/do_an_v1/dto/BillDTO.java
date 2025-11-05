package org.example.do_an_v1.dto;

import lombok.*;
import org.example.do_an_v1.enums.StatusBill;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillDTO {

    private Long id;                      // ID hóa đơn
    private String code;                  // Mã đơn (unique code)
    private StatusBill status;            // Trạng thái hóa đơn (PAYMENT_PENDING, SUCCEED, ...)
    private LocalDateTime createdAt;      // Thời gian tạo đơn
    private LocalDateTime updatedAt;      // Cập nhật gần nhất
    private LocalDateTime checkIn;        // Ngày check-in
    private LocalDateTime checkOut;       // Ngày check-out
    private LocalDateTime actualCheckin;  // Thời gian check-in thực tế (nếu có)

    // --- Thông tin khách hàng ---
    private CustomerDTO customerDTO;

    // --- Thông tin homestay ---
    private HomestaySummaryDTO homestayDTO;


    private CustomerBookingInfoDTO customerBookingInfoDTO;

    // --- Thông tin thanh toán ---
    private List<TransactionDTO> transactions;  // Danh sách giao dịch liên quan
}