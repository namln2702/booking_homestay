package org.example.do_an_v1.service.support;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.do_an_v1.configuration.VNPayConfig;
import org.example.do_an_v1.dto.request.VNPayReturnRequest;
import org.example.do_an_v1.entity.Bill;
import org.example.do_an_v1.entity.Transaction;
import org.example.do_an_v1.repository.TransactionRepository;
import org.example.do_an_v1.utils.VNPayUtil;
import org.springframework.stereotype.Component;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class VNPayVerifySupport {

    private final VNPayConfig vnPayConfig;
    private final TransactionRepository transactionRepository;

    public VNPayVerifyResult verifyPayment(VNPayReturnRequest request) {
        log.info("Verifying VNPay payment: orderId={}, responseCode={}",
                request.getVnp_TxnRef(), request.getVnp_ResponseCode());

        Transaction transaction = transactionRepository.findByOrderId(request.getVnp_TxnRef())
                .orElse(null);
        if (transaction == null) {
            return VNPayVerifyResult.builder()
                    .signatureValid(false)
                    .amountValid(false)
                    .paymentSuccess(false)
                    .orderId(request.getVnp_TxnRef())
                    .errorMessage("Transaction not found")
                    .build();
        }

        Bill bill = transaction.getBill();

        Map<String, String> params = buildParamsMap(request);

        if (!validateRequiredParams(params, request.getVnp_TxnRef())) {
            return VNPayVerifyResult.builder()
                    .transaction(transaction)
                    .bill(bill)
                    .signatureValid(false)
                    .amountValid(false)
                    .paymentSuccess(false)
                    .orderId(request.getVnp_TxnRef())
                    .errorMessage("Missing required VNPay parameters")
                    .build();
        }

        boolean signatureValid = verifySignature(params, request.getVnp_TxnRef());
        String amountError = validateAmount(request, transaction);
        boolean amountValid = amountError == null;
        boolean paymentSuccess = "00".equals(request.getVnp_ResponseCode()) &&
                "00".equals(request.getVnp_TransactionStatus());

        VNPayVerifyResult.VNPayVerifyResultBuilder builder = VNPayVerifyResult.builder()
                .transaction(transaction)
                .bill(bill)
                .signatureValid(signatureValid)
                .amountValid(amountValid)
                .paymentSuccess(paymentSuccess)
                .responseCode(request.getVnp_ResponseCode())
                .transactionStatus(request.getVnp_TransactionStatus())
                .orderId(request.getVnp_TxnRef());

        if (!signatureValid) {
            builder.errorMessage("Invalid signature");
        } else if (!amountValid) {
            builder.errorMessage(amountError);
        }

        VNPayVerifyResult result = builder.build();

        if (result.isVerifySuccess()) {
            log.info("VNPay verify SUCCESS for orderId={}", request.getVnp_TxnRef());
        } else {
            log.warn("VNPay verify FAILED for orderId={} (signature={}, amount={})",
                    request.getVnp_TxnRef(), signatureValid, amountValid);
        }

        return result;
    }

    private Map<String, String> buildParamsMap(VNPayReturnRequest request) {
        Map<String, String> params = new HashMap<>();

        put(params, "vnp_TmnCode", request.getVnp_TmnCode());
        put(params, "vnp_TxnRef", request.getVnp_TxnRef());
        put(params, "vnp_ResponseCode", request.getVnp_ResponseCode());
        put(params, "vnp_TransactionStatus", request.getVnp_TransactionStatus());
        put(params, "vnp_Amount", request.getVnp_Amount());
        put(params, "vnp_BankCode", request.getVnp_BankCode());
        put(params, "vnp_OrderInfo", request.getVnp_OrderInfo());
        put(params, "vnp_TransactionNo", request.getVnp_TransactionNo());
        put(params, "vnp_SecureHash", request.getVnp_SecureHash());

        put(params, "vnp_BankTranNo", request.getVnp_BankTranNo());
        put(params, "vnp_CardType", request.getVnp_CardType());
        put(params, "vnp_PayDate", request.getVnp_PayDate());

        return params;
    }

    private void put(Map<String, String> map, String key, String value) {
        if (value != null && !value.isEmpty()) {
            map.put(key, URLDecoder.decode(value, StandardCharsets.UTF_8));
        }
    }

    private boolean validateRequiredParams(Map<String, String> params, String orderId) {
        String[] required = {
                "vnp_TmnCode", "vnp_TxnRef", "vnp_ResponseCode",
                "vnp_TransactionStatus", "vnp_Amount", "vnp_BankCode",
                "vnp_OrderInfo", "vnp_TransactionNo", "vnp_SecureHash"
        };

        for (String key : required) {
            if (!params.containsKey(key)) {
                log.error("[VNPay] Missing param {} for orderId={}", key, orderId);
                return false;
            }
        }
        return true;
    }

    private boolean verifySignature(Map<String, String> params, String orderId) {
        log.debug("Verify signature for orderId={}, params={}", orderId, params);
        return VNPayUtil.verifyPayment(params, vnPayConfig.getSecretKey());
    }

    private String validateAmount(VNPayReturnRequest request, Transaction transaction) {
        try {
            long amount = Long.parseLong(request.getVnp_Amount()) / 100;
            if (amount != transaction.getAmount().longValue()) {
                return "Amount mismatch. Expected: " + transaction.getAmount() +
                        ", received: " + amount;
            }
            return null;
        } catch (Exception e) {
            return "Invalid vnp_Amount format";
        }
    }
}
