package org.example.do_an_v1.dto;

import lombok.*;
import org.example.do_an_v1.enums.Status;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplaintDTO {

    private Long id;                // ID từ BaseEntity
    private String token;           // Mã token khiếu nại
    private String description;
    private Status statusToken;     // Trạng thái khiếu nại
    private LocalDateTime createdAt;// Thoi gian tao khieu nai

    private Long adminId;           // ID admin xử lý (trích từ entity Admin)
    private String adminName;       // Tên admin (tuỳ chọn nếu muốn hiển thị)

    private Long billId;            // Id cua bill

    private List<String> imageUrls; // Danh sách URL hoặc tên ảnh (listImage)
}
