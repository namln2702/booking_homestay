package org.example.do_an_v1.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload used when a customer cancels an active complaint.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CancelComplaintRequest {

    @Size(max = 1000, message = "Reason cannot exceed 1000 characters")
    private String reason;
}
