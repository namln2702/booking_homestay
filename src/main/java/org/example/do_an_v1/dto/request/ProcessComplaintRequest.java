package org.example.do_an_v1.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO cho host xử lý khiếu nại
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessComplaintRequest {
    
    @NotNull(message = "Complaint ID is required")
    private Long complaintId;
    
    @NotNull(message = "Approval status is required")
    private Boolean approved; // true = đồng ý, false = không đồng ý
}

