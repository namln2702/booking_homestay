package org.example.do_an_v1.service.impl;

import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.do_an_v1.dto.payment.PaymentQrCheckData;
import org.example.do_an_v1.dto.payment.PaymentQrCheckResult;
import org.example.do_an_v1.service.PaymentQrValidationService;
import org.example.do_an_v1.utils.CrcValidator;
import org.example.do_an_v1.utils.EmvCoParser;
import org.example.do_an_v1.utils.QrDecoderUtil;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentQrValidationServiceImpl implements PaymentQrValidationService {

    private static final String PAYMENT_PREFIX = "000201";
    private static final Set<String> REQUIRED_TAGS = Set.of("00", "38", "53", "58", "63");

    @Override
    public PaymentQrCheckResult validatePaymentQr(String imageUrl) {
        BufferedImage bufferedImage = QrDecoderUtil.downloadImage(imageUrl);
        List<String> qrPayloads = QrDecoderUtil.decodeQrPayloads(bufferedImage);

        if (qrPayloads.isEmpty()) {
            PaymentQrCheckData data = PaymentQrCheckData.builder()
                    .isQr(false)
                    .isPaymentQr(false)
                    .isValidStructure(false)
                    .isValidCRC(false)
                    .build();
            return PaymentQrCheckResult.builder()
                    .data(data)
                    .message("No QR code detected")
                    .build();
        }

        // Business rule: inspect every decoded QR and validate the first payload that follows VietQR prefix 000201.
        for (String rawPayload : qrPayloads) {
            if (rawPayload == null) {
                continue;
            }
            String normalizedPayload = rawPayload.trim();
            if (!normalizedPayload.startsWith(PAYMENT_PREFIX)) {
                continue;
            }

            PaymentQrCheckData paymentData = analyzePaymentPayload(normalizedPayload);
            String message = resolveMessage(paymentData);
            return PaymentQrCheckResult.builder()
                    .data(paymentData)
                    .message(message)
                    .build();
        }

        PaymentQrCheckData data = PaymentQrCheckData.builder()
                .isQr(true)
                .isPaymentQr(false)
                .isValidStructure(false)
                .isValidCRC(false)
                .build();
        return PaymentQrCheckResult.builder()
                .data(data)
                .message("QR code is not a payment QR")
                .build();
    }

    private PaymentQrCheckData analyzePaymentPayload(String payload) {
        Map<String, String> tlvData;
        boolean parsedSuccessfully;
        try {
            tlvData = EmvCoParser.parse(payload);
            parsedSuccessfully = REQUIRED_TAGS.stream().allMatch(tlvData::containsKey);
        } catch (IllegalArgumentException ex) {
            log.warn("Failed to parse EMV payload: {}", ex.getMessage());
            return PaymentQrCheckData.builder()
                    .isQr(true)
                    .isPaymentQr(true)
                    .isValidStructure(false)
                    .isValidCRC(false)
                    .build();
        }

        if (!parsedSuccessfully) {
            log.warn("EMV payload missing required tags: {}", payload);
            return PaymentQrCheckData.builder()
                    .isQr(true)
                    .isPaymentQr(true)
                    .isValidStructure(false)
                    .isValidCRC(false)
                    .build();
        }

        boolean crcValid = false;
        String crc = tlvData.get("63");
        if (crc != null) {
            crcValid = CrcValidator.isValid(payload, crc);
        }

        BigDecimal amount = extractAmount(tlvData.get("54"));
        String bankCode = extractBankCode(tlvData.get("38"));

        return PaymentQrCheckData.builder()
                .isQr(true)
                .isPaymentQr(true)
                .isValidStructure(true)
                .isValidCRC(crcValid)
                .amount(amount)
                .bankCode(bankCode)
                .build();
    }

    private BigDecimal extractAmount(String rawAmount) {
        if (rawAmount == null || rawAmount.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(rawAmount);
        } catch (NumberFormatException ex) {
            log.warn("Amount tag cannot be parsed: {}", rawAmount);
            return null;
        }
    }

    private String extractBankCode(String merchantAccountInfo) {
        if (merchantAccountInfo == null || merchantAccountInfo.isBlank()) {
            return null;
        }

        try {
            Map<String, String> merchantInfo = EmvCoParser.parse(merchantAccountInfo);
            // VietQR typically stores BIN at tag "01". Fall back to "00" if only the AID is available.
            String bin = merchantInfo.get("01");
            if (bin != null && !bin.isBlank()) {
                return bin.toUpperCase(Locale.ROOT);
            }
            String guid = merchantInfo.get("00");
            if (guid != null && !guid.isBlank()) {
                return guid.toUpperCase(Locale.ROOT);
            }
        } catch (IllegalArgumentException ex) {
            log.warn("Unable to parse merchant account info: {}", ex.getMessage());
        }
        return null;
    }

    // Business rule: expose human readable reason for the validation result.
    private String resolveMessage(PaymentQrCheckData data) {
        if (!data.isValidStructure()) {
            return "Invalid EMVCo structure";
        }
        if (!data.isValidCRC()) {
            return "Invalid CRC checksum";
        }
        return "Valid VietQR payment code";
    }
}
