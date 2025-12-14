package org.example.do_an_v1.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request để tạo VNPay payment URL
 * Có thể truyền billId hoặc transactionId
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePaymentUrlRequest {
    
    /**
     * ID của bill (tùy chọn nếu có transactionId)
     * Nếu chỉ có billId, sẽ tự động tìm transaction PENDING
     */
    private Long billId;
    
    /**
     * ID của transaction cụ thể (tùy chọn nếu có billId)
     * Nếu có transactionId, sẽ tạo payment URL cho transaction đó
     */
    private Long transactionId;
}

