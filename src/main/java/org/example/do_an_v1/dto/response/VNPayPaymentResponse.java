package org.example.do_an_v1.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VNPayPaymentResponse {
    private String paymentUrl;
    private String orderId;
    private Long amount;
    private String message;
}

