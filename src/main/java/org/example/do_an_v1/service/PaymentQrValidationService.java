package org.example.do_an_v1.service;

import org.example.do_an_v1.dto.payment.PaymentQrCheckResult;

public interface PaymentQrValidationService {

    PaymentQrCheckResult validatePaymentQr(String imageUrl);
}
