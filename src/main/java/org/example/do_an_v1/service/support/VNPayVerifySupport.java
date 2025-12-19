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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Component hỗ trợ xác thực thanh toán VNPay
 * CHỈ làm việc verify signature và validate amount
 * Nghiệp vụ xử lý (update status, unlock homestay, send email) sẽ được xử lý riêng
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VNPayVerifySupport {

    private final VNPayConfig vnPayConfig;
    private final TransactionRepository transactionRepository;

    /**
     * Verify payment từ VNPay
     * CHỈ làm việc verify signature và validate amount
     * KHÔNG xử lý nghiệp vụ (update status, unlock homestay, send email)
     *
     * @param request VNPayReturnRequest từ FE
     * @return VNPayVerifyResult chứa kết quả verify
     * @throws IllegalArgumentException nếu request không hợp lệ hoặc transaction không tồn tại
     */
    public VNPayVerifyResult verifyPayment(VNPayReturnRequest request) {
        log.info("Verifying payment: orderId={}, responseCode={}",
                request.getVnp_TxnRef(), request.getVnp_ResponseCode());

        // Tra cứu transaction bằng orderId
        Optional<Transaction> transactionOpt = transactionRepository.findByOrderId(request.getVnp_TxnRef());
        if (transactionOpt.isEmpty()) {
            log.error("Transaction not found with orderId: {}", request.getVnp_TxnRef());
            return VNPayVerifyResult.builder()
                    .signatureValid(false)
                    .amountValid(false)
                    .paymentSuccess(false)
                    .orderId(request.getVnp_TxnRef())
                    .errorMessage("Transaction not found with orderId: " + request.getVnp_TxnRef())
                    .build();
        }

        Transaction transaction = transactionOpt.get();
        Bill bill = transaction.getBill();

        // Build params map để verify signature
        Map<String, String> params = buildParamsMap(request);

        // Validate required parameters
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

        // Verify signature
        boolean signatureValid = verifySignature(params, request.getVnp_TxnRef());

        // Validate amount
        boolean amountValid = false;
        String amountError = null;
        String validationError = validateAmount(request, transaction);
        if (validationError == null) {
            amountValid = true;
        } else {
            amountError = validationError;
            log.warn("Amount validation failed for orderId: {}. Error: {}",
                    request.getVnp_TxnRef(), amountError);
        }

        // Kiểm tra payment có thành công không
        boolean paymentSuccess = "00".equals(request.getVnp_ResponseCode())
                && "00".equals(request.getVnp_TransactionStatus());

        // Build result
        VNPayVerifyResult.VNPayVerifyResultBuilder resultBuilder = VNPayVerifyResult.builder()
                .transaction(transaction)
                .bill(bill)
                .signatureValid(signatureValid)
                .amountValid(amountValid)
                .paymentSuccess(paymentSuccess)
                .responseCode(request.getVnp_ResponseCode())
                .transactionStatus(request.getVnp_TransactionStatus())
                .orderId(request.getVnp_TxnRef());

        if (!signatureValid) {
            resultBuilder.errorMessage("Invalid signature");
        } else if (!amountValid) {
            resultBuilder.errorMessage(amountError);
        }

        VNPayVerifyResult result = resultBuilder.build();

        if (result.isVerifySuccess()) {
            log.info("Payment verified successfully for orderId: {} (signature: {}, amount: {}, payment: {})",
                    request.getVnp_TxnRef(), signatureValid, amountValid, paymentSuccess);
        } else {
            log.warn("Payment verification failed for orderId: {} (signature: {}, amount: {})",
                    request.getVnp_TxnRef(), signatureValid, amountValid);
        }

        return result;
    }

    /**
     * Build params map từ VNPayReturnRequest
     */
    private Map<String, String> buildParamsMap(VNPayReturnRequest request) {
        Map<String, String> params = new HashMap<>();

        // Các tham số BẮT BUỘC - chỉ thêm nếu có giá trị
        if (request.getVnp_TmnCode() != null && !request.getVnp_TmnCode().isEmpty()) {
            params.put("vnp_TmnCode", request.getVnp_TmnCode());
        }
        if (request.getVnp_TxnRef() != null && !request.getVnp_TxnRef().isEmpty()) {
            params.put("vnp_TxnRef", request.getVnp_TxnRef());
        }
        if (request.getVnp_ResponseCode() != null && !request.getVnp_ResponseCode().isEmpty()) {
            params.put("vnp_ResponseCode", request.getVnp_ResponseCode());
        }
        if (request.getVnp_TransactionStatus() != null && !request.getVnp_TransactionStatus().isEmpty()) {
            params.put("vnp_TransactionStatus", request.getVnp_TransactionStatus());
        }
        if (request.getVnp_Amount() != null && !request.getVnp_Amount().isEmpty()) {
            params.put("vnp_Amount", request.getVnp_Amount());
        }
        if (request.getVnp_BankCode() != null && !request.getVnp_BankCode().isEmpty()) {
            params.put("vnp_BankCode", request.getVnp_BankCode());
        }
        if (request.getVnp_OrderInfo() != null && !request.getVnp_OrderInfo().isEmpty()) {
            params.put("vnp_OrderInfo", request.getVnp_OrderInfo());
        }
        if (request.getVnp_TransactionNo() != null && !request.getVnp_TransactionNo().isEmpty()) {
            params.put("vnp_TransactionNo", request.getVnp_TransactionNo());
        }

        // vnp_SecureHash phải được thêm vào để verifyPayment có thể lấy ra
        if (request.getVnp_SecureHash() != null && !request.getVnp_SecureHash().isEmpty()) {
            params.put("vnp_SecureHash", request.getVnp_SecureHash());
        }

        // Các tham số TÙY CHỌN
        if (request.getVnp_BankTranNo() != null && !request.getVnp_BankTranNo().isEmpty()) {
            params.put("vnp_BankTranNo", request.getVnp_BankTranNo());
        }
        if (request.getVnp_CardType() != null && !request.getVnp_CardType().isEmpty()) {
            params.put("vnp_CardType", request.getVnp_CardType());
        }
        if (request.getVnp_PayDate() != null && !request.getVnp_PayDate().isEmpty()) {
            params.put("vnp_PayDate", request.getVnp_PayDate());
        }

        return params;
    }

    /**
     * Validate các tham số bắt buộc
     * @return true nếu hợp lệ, false nếu thiếu tham số
     */
    private boolean validateRequiredParams(Map<String, String> params, String orderId) {
        if (!params.containsKey("vnp_TmnCode") || !params.containsKey("vnp_TxnRef")
                || !params.containsKey("vnp_ResponseCode") || !params.containsKey("vnp_TransactionStatus")
                || !params.containsKey("vnp_Amount") || !params.containsKey("vnp_BankCode")
                || !params.containsKey("vnp_OrderInfo") || !params.containsKey("vnp_TransactionNo")
                || !params.containsKey("vnp_SecureHash")) {
            log.error("Missing required parameters for orderId: {}. Params: {}", orderId, params.keySet());
            return false;
        }
        return true;
    }

    /**
     * Verify signature
     */
    private boolean verifySignature(Map<String, String> params, String orderId) {
        log.debug("Verifying payment signature for orderId: {}, params: {}", orderId, params);
        return VNPayUtil.verifyPayment(params, vnPayConfig.getSecretKey());
    }

    /**
     * Validate amount
     * @return null nếu hợp lệ, error message nếu không hợp lệ
     */
    private String validateAmount(VNPayReturnRequest request, Transaction transaction) {
        long amountInVnd;
        try {
            String vnpAmount = request.getVnp_Amount();
            if (vnpAmount == null || vnpAmount.isEmpty()) {
                return "vnp_Amount is null or empty";
            }
            amountInVnd = Long.parseLong(vnpAmount) / 100; // VNPay trả về amount tính bằng xu
        } catch (NumberFormatException e) {
            log.error("Invalid vnp_Amount format for orderId: {}. Value: {}",
                    request.getVnp_TxnRef(), request.getVnp_Amount(), e);
            return "Invalid vnp_Amount format: " + e.getMessage();
        }

        if (amountInVnd != transaction.getAmount().longValue()) {
            log.error("Amount mismatch. Expected: {} VND, Received: {} VND (from vnp_Amount: {})",
                    transaction.getAmount(), amountInVnd, request.getVnp_Amount());
            return "Amount mismatch. Expected: " + transaction.getAmount() +
                    " VND, Received: " + amountInVnd + " VND";
        }

        return null; // Valid
    }

}