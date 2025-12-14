package org.example.do_an_v1.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.do_an_v1.dto.ComplaintDTO;
import org.example.do_an_v1.enums.StatusBill;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO response cho API lấy danh sách bills với complaint
 * Chỉ chứa thông tin cơ bản của bill và complaint
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillComplaintResponse {
    
    // Bill info
    private Long billId;
    private String billCode;
    private StatusBill billStatus;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private LocalDateTime actualCheckinTime;
    private BigDecimal totalAmount;
    private LocalDateTime billCreatedAt;
    
    // Complaint info
    private ComplaintDTO complaint;
}

