package org.example.do_an_v1.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.do_an_v1.dto.payment.PaymentQrCheckRequest;
import org.example.do_an_v1.dto.payment.PaymentQrCheckResponse;
import org.example.do_an_v1.dto.payment.PaymentQrCheckResult;
import org.example.do_an_v1.exception.PaymentQrProcessingException;
import org.example.do_an_v1.service.PaymentQrValidationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class PaymentQrValidationController {

    private final PaymentQrValidationService paymentQrValidationService;

    @PostMapping("/check-payment-qr")
    public ResponseEntity<PaymentQrCheckResponse> checkPaymentQr(
            @Valid @RequestBody PaymentQrCheckRequest request) {
        try {
            PaymentQrCheckResult result = paymentQrValidationService.validatePaymentQr(request.getImageUrl());
            PaymentQrCheckResponse response = PaymentQrCheckResponse.builder()
                    .success(true)
                    .message(result.getMessage())
                    .data(result.getData())
                    .build();
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            PaymentQrCheckResponse response = PaymentQrCheckResponse.builder()
                    .success(false)
                    .message(ex.getMessage())
                    .build();
            return ResponseEntity.badRequest().body(response);
        } catch (PaymentQrProcessingException ex) {
            PaymentQrCheckResponse response = PaymentQrCheckResponse.builder()
                    .success(false)
                    .message(ex.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
