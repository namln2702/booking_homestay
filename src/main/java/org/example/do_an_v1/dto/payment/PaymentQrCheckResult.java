package org.example.do_an_v1.dto.payment;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PaymentQrCheckResult {
    PaymentQrCheckData data;
    String message;
}
