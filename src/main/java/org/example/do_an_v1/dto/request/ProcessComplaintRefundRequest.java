package org.example.do_an_v1.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessComplaintRefundRequest {
    @NotNull(message = "Complaint ID is required")
    private Long complaintId;

    @NotNull(message = "Approval decision is required")
    private Boolean approved;

}

