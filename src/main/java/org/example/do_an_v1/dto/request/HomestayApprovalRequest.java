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
public class HomestayApprovalRequest {

    @NotNull(message = "Homestay ID is required")
    private Long homestayId;

    /**
     * true  -> duyệt, chuyển sang ACTIVE
     * false -> từ chối, chuyển sang INACTIVE
     */
    @NotNull(message = "Approval decision is required")
    private Boolean approve;
}


