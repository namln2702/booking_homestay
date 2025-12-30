package org.example.do_an_v1.dto.payment;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PaymentQrCheckData {
    boolean isQr;
    boolean isPaymentQr;
    boolean isValidStructure;
    boolean isValidCRC;
    BigDecimal amount;
    String bankCode;
}
