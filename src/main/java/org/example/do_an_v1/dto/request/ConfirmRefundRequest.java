package org.example.do_an_v1.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmRefundRequest {
    @NotNull(message = "Transaction ID is required")
    private Long transactionId;
    
    @NotBlank(message = "Proof image URL is required")
    private String proofImageUrl; // URL hình ảnh chứng minh giao dịch đã được thực hiện
}

