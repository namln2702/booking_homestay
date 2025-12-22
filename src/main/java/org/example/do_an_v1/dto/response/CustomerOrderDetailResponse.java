package org.example.do_an_v1.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerOrderDetailResponse {
    private CustomerOrderResponse summary;
    private CustomerOrderPaymentStatusResponse paymentStatus;
    private CustomerOrderComplaintStatusResponse complaintStatus;
    private CustomerOrderActionPermissionResponse actions;
}
