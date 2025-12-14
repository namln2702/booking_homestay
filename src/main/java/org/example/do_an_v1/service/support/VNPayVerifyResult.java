package org.example.do_an_v1.service.support;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.do_an_v1.entity.Bill;
import org.example.do_an_v1.entity.Transaction;

/**
 * Kết quả verify payment từ VNPay
 * Chỉ chứa thông tin verify (signature, amount), không chứa nghiệp vụ
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VNPayVerifyResult {
    /**
     * Transaction được verify
     */
    private Transaction transaction;
    
    /**
     * Bill liên kết với transaction
     */
    private Bill bill;
    
    /**
     * Signature có hợp lệ không
     */
    private boolean signatureValid;
    
    /**
     * Amount có khớp không
     */
    private boolean amountValid;
    
    /**
     * Payment có thành công không (responseCode = "00" && transactionStatus = "00")
     */
    private boolean paymentSuccess;
    
    /**
     * Response code từ VNPay
     */
    private String responseCode;
    
    /**
     * Transaction status từ VNPay
     */
    private String transactionStatus;
    
    /**
     * Order ID (vnp_TxnRef)
     */
    private String orderId;
    
    /**
     * Lỗi nếu có (signature invalid, amount mismatch, etc.)
     */
    private String errorMessage;
    
    /**
     * Kiểm tra verify có thành công không (signature valid và amount valid)
     */
    public boolean isVerifySuccess() {
        return signatureValid && amountValid;
    }
}

