package org.example.do_an_v1.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.do_an_v1.dto.ComplaintDTO;
import org.example.do_an_v1.enums.StatusBill;

import java.time.LocalDateTime;

/**
 * Response payload for the customer's "My Complaints" screen.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerComplaintResponse {
    private Long billId;
    private String billCode;
    private StatusBill billStatus;
    private Long homestayId;
    private String homestayName;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private LocalDateTime billCreatedAt;
    private ComplaintDTO complaint;
}
