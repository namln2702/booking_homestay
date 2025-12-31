package org.example.do_an_v1.dto.payment;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PaymentQrCheckResponse {
    boolean success;
    String message;
    PaymentQrCheckData data;
}
