package org.example.do_an_v1.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO nhận thông tin từ FE sau khi customer thanh toán xong
 * FE sẽ gửi các tham số từ VNPay return URL
 * 
 * Theo tài liệu VNPay, các tham số bắt buộc:
 * - vnp_TmnCode, vnp_Amount, vnp_BankCode, vnp_OrderInfo, 
 *   vnp_TransactionNo, vnp_ResponseCode, vnp_TransactionStatus, 
 *   vnp_TxnRef, vnp_SecureHash
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VNPayReturnRequest {
    
    // ========== Các tham số BẮT BUỘC ==========
    
    @NotBlank(message = "vnp_TmnCode is required")
    private String vnp_TmnCode;             // Mã website của merchant (bắt buộc)
    
    @NotBlank(message = "vnp_TxnRef is required")
    private String vnp_TxnRef;              // Mã tham chiếu giao dịch (orderId) - bắt buộc
    
    @NotBlank(message = "vnp_ResponseCode is required")
    private String vnp_ResponseCode;         // Mã phản hồi: "00" = thành công - bắt buộc
    
    @NotBlank(message = "vnp_TransactionStatus is required")
    private String vnp_TransactionStatus;   // Trạng thái giao dịch: "00" = thành công - bắt buộc
    
    @NotBlank(message = "vnp_Amount is required")
    private String vnp_Amount;              // Số tiền (đã x100, tính bằng xu) - bắt buộc
    
    @NotBlank(message = "vnp_BankCode is required")
    private String vnp_BankCode;            // Mã Ngân hàng thanh toán - bắt buộc
    
    @NotBlank(message = "vnp_OrderInfo is required")
    private String vnp_OrderInfo;           // Thông tin mô tả nội dung thanh toán - bắt buộc
    
    @NotBlank(message = "vnp_TransactionNo is required")
    private String vnp_TransactionNo;       // Mã giao dịch tại VNPay - bắt buộc
    
    @NotBlank(message = "vnp_SecureHash is required")
    private String vnp_SecureHash;          // Chữ ký để xác thực - bắt buộc
    
    // ========== Các tham số TÙY CHỌN ==========
    
    private String vnp_BankTranNo;          // Mã giao dịch tại Ngân hàng (tùy chọn)
    private String vnp_CardType;            // Loại tài khoản/thẻ: ATM, QRCODE (tùy chọn)
    private String vnp_PayDate;             // Thời gian thanh toán yyyyMMddHHmmss (tùy chọn)
}

