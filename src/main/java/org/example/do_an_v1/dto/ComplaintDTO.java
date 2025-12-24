package org.example.do_an_v1.dto;

import lombok.*;
import org.example.do_an_v1.enums.StatusBill;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplaintDTO {

    private Long id;                // ID từ BaseEntity
//    private String token;           // Mã token khiếu nại
    private String description;
    private LocalDateTime createdAt;// Thoi gian tao khieu nai

    private String adminName;      // Tên admin xử lý

    private Long billId;            // Id cua bill
    private StatusBill billStatus;  // Trạng thái của bill

    private String homestayTitle;   // Tên homestay được khiếu nại

    private List<String> imageUrls; // Danh sách URL hoặc tên ảnh (listImage)

    private TransactionDTO transaction; // Transaction REFUND liên quan đến complaint
}
